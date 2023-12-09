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
import java.util.Random;

public class WorkerBehaviour extends AbstractBehavior<WorkerBehaviour.Command> {


    @Getter
    @Setter
    @AllArgsConstructor
    public static class Command implements Serializable {
        private static final long serialVersionUIID = 1L;
        private String message;
        private ActorRef<ManagerBehavior.Command> sender;
    }


    private WorkerBehaviour(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehaviour::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return handleMessageWhenWeDontYetHaveAPrimerNumber();
    }

    public Receive<Command> handleMessageWhenWeDontYetHaveAPrimerNumber() {
        return newReceiveBuilder()
                .onAnyMessage(command -> {
                    BigInteger bigInteger = new BigInteger(2000, new Random());
                    BigInteger prime = bigInteger.nextProbablePrime();
                    Random r = new Random();
                    if (r.nextInt(5) < 2) {
                        command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
                    }
                    return handleMessageWhenWeAlreadyHaveAPromerNumber(prime);

                })
                .build();
    }

    public Receive<Command> handleMessageWhenWeAlreadyHaveAPromerNumber(BigInteger prime) {
        return newReceiveBuilder()
                .onAnyMessage(command -> {
                    Random r = new Random();
                    if (r.nextInt(5) < 2) {
                        command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
                    }
                    return Behaviors.same();
                })
                .build();
    }
}
