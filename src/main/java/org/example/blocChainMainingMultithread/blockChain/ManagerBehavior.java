package org.example.blocChainMainingMultithread.blockChain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.blocChainMainingMultithread.model.Block;
import org.example.blocChainMainingMultithread.model.HashResult;

import java.io.Serializable;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public interface Command extends Serializable {}

    @Getter
    @Setter
    @AllArgsConstructor
    public static class MineBlockCommand implements Command {
        private static final long serialVersionUID = 1L;
        private Block block;
        private ActorRef<HashResult> sender;
        private int difficulty;
//        private ActorRef<ManagerBehavior.Command> controller;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class HashResultCommand implements Command {
        private static final long serialVersionUID = 1L;
        private HashResult hashResult;
    }

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onSignal(Terminated.class, handler -> {
                    startNextWorker();
                    return Behaviors.same();
                })
                .onMessage(MineBlockCommand.class, message -> {
                    this.sender = message.getSender();
                    this.block = message.getBlock();
                    this.difficulty = message.getDifficulty();
                    this.currentlyMining = true;
                    for (int i = 0; i < 10; i++) {
                        startNextWorker();
                    }
                    return Behaviors.same();
                })
                .onMessage(HashResultCommand.class, message -> {
                    for (ActorRef<Void> child : getContext().getChildren()) {
                        getContext().stop(child);
                    }
                    this.currentlyMining = false;
                    sender.tell(message.getHashResult());
                    return Behaviors.same();
                })
                .build();
    }

    private ActorRef<HashResult> sender;
    private Block block;
    private int difficulty;
    private int currentNonce = 0;
    private boolean currentlyMining;

    private void startNextWorker() {
        if (currentlyMining) {
            //System.out.println("About to start mining with nonces starting at " + currentNonce * 1000);

            Behavior<WorkerBehavior.Command> workerBehavior = Behaviors.supervise(WorkerBehavior.create()).onFailure(SupervisorStrategy.resume());

            ActorRef<WorkerBehavior.Command> worker = getContext().spawn(workerBehavior, "worker" + currentNonce);
            getContext().watch(worker);
            worker.tell(new WorkerBehavior.Command(block, currentNonce * 1000, difficulty, getContext().getSelf()));
            currentNonce++;
        }
    }
}
