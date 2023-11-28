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

    private BigInteger prime;

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(command -> {
                    if (command.getMessage().equals("start")) {
                        if (prime == null) {
                            BigInteger bigInteger = new BigInteger(5000, new Random());
                            prime = bigInteger.nextProbablePrime();
                        }
                        command.getSender().tell(new ManagerBehavior.ResultCommand(prime));
                    }
                    return this;
                })
                .build();
    }
}
