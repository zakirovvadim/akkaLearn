package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.TreeSet;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public interface Command extends Serializable {
    }

    ;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class InstructionCommand implements Command {
        public static final long serialVersionalUID = 1L;
        private String message;
        private ActorRef<SortedSet<BigInteger>> sender;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ResultCommand implements Command {
        public static final long serialVersionalUID = 1L;
        private BigInteger prime;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class NoResponseReceivedCommand implements Command {
        public static final long serialVersionalUID = 1L;
        private ActorRef<WorkerBehaviour.Command> worker;
    }

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    private SortedSet<BigInteger> primes = new TreeSet<>();

    private ActorRef<SortedSet<BigInteger>> sender;

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, command -> {
                    if (command.getMessage().equals("start")) {
                        this.sender = command.getSender();
                        for (int i = 0; i < 20; i++) {
                            ActorRef<WorkerBehaviour.Command> worker = getContext().spawn(WorkerBehaviour.create(), "worker" + i);
                            worker.tell(new WorkerBehaviour.Command("start", getContext().getSelf()));
                            askWorkerForAPrime(worker);
                        }
                    }
                    return Behaviors.same();
                })
                .onMessage(ResultCommand.class, command -> {
                    primes.add(command.getPrime());
                    System.out.println("I have received " + primes.size() + " prime numbers");
                    if (primes.size() == 20) {
                        this.sender.tell(primes);
                    }
                    return Behaviors.same();
                })
                .onMessage(NoResponseReceivedCommand.class, command -> {
                    System.out.println("Retrying with worker " + command.getWorker().path());
                    askWorkerForAPrime(command.getWorker());
                    return Behaviors.same();
                })
                .build();
    }

    private void askWorkerForAPrime(ActorRef<WorkerBehaviour.Command> worker) {
        getContext().ask(Command.class,
                worker,
                Duration.ofSeconds(5L),
                (me) -> new WorkerBehaviour.Command("start", me),
                (response, throwablwe) -> {
                    if (response != null) {
                        return response;
                    } else {
                        System.out.println("Worker " + worker.path() + " failed to respond.");
                        return new NoResponseReceivedCommand(worker);
                    }
                });
    }
}
