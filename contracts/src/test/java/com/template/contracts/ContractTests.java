package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;
import static net.corda.testing.node.NodeTestUtils.transaction;


public class ContractTests {
 private final TestIdentity Mint = new TestIdentity(new CordaX500Name("mint", "","GB"));
 private final TestIdentity TraderA = new TestIdentity(new CordaX500Name("traderA", "", "GB"));
 private final TestIdentity TraderB = new TestIdentity(new CordaX500Name("traderB", "", "GB"));

 private final MockServices ledgerServices = new MockServices();

 private MetalState metalState = new MetalState("Gold",10,Mint.getParty(), TraderA.getParty());
    private MetalState metalStateInput = new MetalState("Gold",10,Mint.getParty(), TraderA.getParty());
    private MetalState metalStateOutput = new MetalState("Gold",10,Mint.getParty(), TraderB.getParty());

    //-----------------------Issue Command---------------------------//

    @Test
    public void metalContractImplementsContract(){
        assert (new MetalContract() instanceof Contract);
    }

    @Test
    public void MetalContractRequiresZeroInputsInIssueTransaction(){
        transaction(ledgerServices, tx -> {
           tx.input(MetalContract.CID, metalState);
           tx.command(Mint.getPublicKey(), new MetalContract.Issue());
           tx.fails();
           return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void MetalContractRequiresOneOutputInIssueTransaction(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void MetalContractRequiresTheTransactionOutputToBeAMetalState(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, new DummyState());
            tx.command(Mint.getPublicKey(), new MetalContract.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void MetalContractRequiresTheTransactionCommandToBeAnIssueCommand(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void MetalContractRequiresTheIssuerToBeARequiredSignerInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(TraderA.getPublicKey(), new MetalContract.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.Issue());
            tx.verifies();
            return null;
        });
    }
    //-----------------------Transfer Command---------------------------//

    @Test
    public void MetalContractRequiresOneInputAndOneOutputInTransferTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.Transfer());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.command(TraderA.getPublicKey(), new MetalContract.Transfer());
            tx.fails();
            return null;
        });
    }

    @Test
    public void MetalContractRequiresTheTransactionCommandToBeATransferCommand(){
        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.Transfer());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void MetalContractRequiresTheOwnerToBeARequiredSigner(){
        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.Transfer());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(Mint.getPublicKey(), new MetalContract.Transfer());
            tx.fails();
            return null;
        });
    }



}