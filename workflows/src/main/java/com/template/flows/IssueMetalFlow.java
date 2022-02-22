package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.contracts.TemplateContract;
import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

    @InitiatingFlow
    @StartableByRPC
    public class IssueMetalFlow extends FlowLogic<SignedTransaction>{

        //private variables
        private String metalName;
        private int weight;
        private Party owner;

        public IssueMetalFlow(String metalName, int weight, Party owner) {
            this.metalName = metalName;
            this.weight = weight;
            this.owner = owner;
        }

        private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retrieving the Notary.");
        private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction.");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
        private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterparty.");
        private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction");

        private final ProgressTracker progressTracker = new ProgressTracker(
                RETRIEVING_NOTARY,
                GENERATING_TRANSACTION,
                SIGNING_TRANSACTION,
                COUNTERPARTY_SESSION,
                FINALISING_TRANSACTION
        );

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }



        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            // Initiator flow logic goes here.

            // Retrieve Notary Identity
            progressTracker.setCurrentStep(RETRIEVING_NOTARY);
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);


            //Create transaction components
            MetalState outputState = new MetalState(metalName, weight, getOurIdentity(), owner);
            Command cmd = new Command(new MetalContract.Issue(), getOurIdentity().getOwningKey());


            // Create transaction builder
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            TransactionBuilder txB = new TransactionBuilder(notary)
                    .addOutputState(outputState, MetalContract.CID)
                    .addCommand(cmd);


            // Sign the transaction
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txB);


            // Create session with CounterParty
            progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
            FlowSession otherPartySession = initiateFlow(owner);


            // Finalize and send to CounterParty
            progressTracker.setCurrentStep(FINALISING_TRANSACTION);
            return subFlow(new FinalityFlow(signedTx, otherPartySession));
        }
    }

