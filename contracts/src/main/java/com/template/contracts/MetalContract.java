package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class MetalContract implements Contract {

    public static final String CID = "com.template.contracts.MetalContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        if(tx.getCommands().size() != 1)
            throw new IllegalArgumentException("Transaction must have one command");

        Command command = tx.getCommand(0);
        CommandData commandType = command.getValue();
        List<PublicKey> requiredSigners = command.getSigners();

        //-----------------Issue Command Contract Rules -------------//
        if(commandType instanceof Issue){
            //Issue Transaction logic

            //Shape Rules
            if(tx.getInputs().size() != 0)
                throw new IllegalArgumentException("Issue cannot have inputs");

            if(tx.getOutputs().size() != 1){
                throw new IllegalArgumentException("Issue can have only one output");
            }

            //Content Rules

            ContractState outputState = tx.getOutput(0);

            if(!(outputState instanceof MetalState)){
                throw new IllegalArgumentException("Output must be a metal state");
            }

            MetalState metalState = (MetalState) outputState;

            if(!metalState.getMetalName().equals("Gold") && !metalState.getMetalName().equals("Silver")){
                throw new IllegalArgumentException("Metal is not Gold or Silver");
            }

            //Signer Rules
            Party issuer = metalState.getIssuer();
            PublicKey issuersKey = issuer.getOwningKey();

            if(!(requiredSigners.contains(issuersKey))){
                throw new IllegalArgumentException("Issuer has to sign the issuance");
            }

        }

        //-----------------Transfer Command Contract Rules -------------//
        else if(commandType instanceof Transfer){
            //Issue Transaction logic

            //Shape Rules
            if(tx.getInputs().size() != 1)
                throw new IllegalArgumentException("Transfer needs to have one input");

            if(tx.getOutputs().size() != 1){
                throw new IllegalArgumentException("Issue can have only one output");
            }

            //Content Rules

            ContractState inputState = tx.getInput(0);
            ContractState outputState = tx.getOutput(0);

            if(!(outputState instanceof MetalState)){
                throw new IllegalArgumentException("Input must be a metal state");
            }

            MetalState metalState = (MetalState) inputState;

            if(!metalState.getMetalName().equals("Gold") && !metalState.getMetalName().equals("Silver")){
                throw new IllegalArgumentException("Metal is not Gold or Silver");
            }

            //Signer Rules
            Party owner = metalState.getOwner();
            PublicKey ownersKey = owner.getOwningKey();

            if(!(requiredSigners.contains(ownersKey))){
                throw new IllegalArgumentException("Owner has to sign the transfer");
            }

        }

        else throw new IllegalArgumentException("Command not found");

    }

    // Used to indicate the transaction's intent.
    public static class Issue implements CommandData {}
    public static class Transfer implements CommandData {}
}