package com.template;

import com.google.common.collect.ImmutableList;
import com.template.contracts.MetalContract;
import com.template.flows.IssueMetalFlow;
import com.template.flows.TemplateFlow;
import com.template.flows.TransferMetalFlow;
import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlowTests {
    private final MockNetwork network = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
            TestCordapp.findCordapp("com.template.contracts"),
            TestCordapp.findCordapp("com.template.flows")
    )));

    private final StartedMockNode Mint = network.createNode();
    private final StartedMockNode A = network.createNode();
    private final StartedMockNode B = network.createNode();

    @Before
    public void setup() { network.runNetwork();}

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void transactionHasNoInputsHasOneMetalStateOutputWithTheCorrectOwner() throws Exception{
        IssueMetalFlow flow = new IssueMetalFlow("Gold",10, A.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = Mint.startFlow(flow);
        setup();
        SignedTransaction signedTransaction = future.get();

        assertEquals(0, signedTransaction.getTx().getInputs().size());

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());;
        MetalState output = signedTransaction.getTx().outputsOfType(MetalState.class).get(0);

        assertEquals(A.getInfo().getLegalIdentities().get(0), output.getOwner());
    }

    @Test
    public void transactionHasTheCorrectContractWithOneIssueCommandAndIssuerAsSigner() throws Exception {

        IssueMetalFlow flow = new IssueMetalFlow("Gold", 10, A.getInfo().getLegalIdentities().get(0));
        CordaFuture<SignedTransaction> future = Mint.startFlow(flow);
        setup();
        SignedTransaction signedTransaction = future.get();

        TransactionState output = signedTransaction.getTx().getOutputs().get(0);
        assertEquals("com.template.contracts.MetalContract", output.getContract());

        assertEquals(1, signedTransaction.getTx().getCommands().size());

        Command command = signedTransaction.getTx().getCommands().get(0);
        assert (command.getValue() instanceof MetalContract.Issue);

        assertEquals(1, command.getSigners().size());
        assertTrue(command.getSigners().contains(Mint.getInfo().getLegalIdentities().get(0).getOwningKey()));
    }

    // ------------------------------------------ Transfer Metal Flow Tests ----------------------------------------



    @Test
    public void transactionHasOneInputAndOneOutput() throws Exception {
        IssueMetalFlow flow = new IssueMetalFlow("Gold", 10, A.getInfo().getLegalIdentities().get(0));
        TransferMetalFlow transferFlow = new TransferMetalFlow("Gold", 10, B.getInfo().getLegalIdentities().get(0));

        CordaFuture<SignedTransaction> future = Mint.startFlow(flow);
        setup();

        CordaFuture<SignedTransaction> transferFuture = A.startFlow(transferFlow);
        setup();

        SignedTransaction signedTransaction = transferFuture.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        assertEquals(1, signedTransaction.getTx().getInputs().size());

    }

    @Test
    public void transactionHasTransferCommandWithOwnerAsSigner() throws Exception {
        IssueMetalFlow flow = new IssueMetalFlow("Gold", 10, A.getInfo().getLegalIdentities().get(0));
        TransferMetalFlow transferFlow = new TransferMetalFlow("Gold", 10, B.getInfo().getLegalIdentities().get(0));

        CordaFuture<SignedTransaction> future = Mint.startFlow(flow);
        setup();

        CordaFuture<SignedTransaction> transferFuture = A.startFlow(transferFlow);
        setup();

        SignedTransaction signedTransaction = transferFuture.get();

        assertEquals(1, signedTransaction.getTx().getCommands().size());
        Command command = signedTransaction.getTx().getCommands().get(0);

        assert (command.getValue() instanceof MetalContract.Transfer);
        assertTrue(command.getSigners().contains(A.getInfo().getLegalIdentities().get(0).getOwningKey()));

    }
}
