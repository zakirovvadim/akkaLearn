package org.example.racer;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    public interface Command extends Serializable {};

    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

    @AllArgsConstructor
    @Getter
    public static class RacerUpdateCommand implements Command {
        private static final long serialVersionUID = 1L;
        private ActorRef<Racer.Command> racer;
        private int position;
    }

    private RaceController(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(RaceController::new);
    }

    private Map<ActorRef<Racer.Command>, Integer> currentPosition;
    private long start;
    private int raceLength = 100;

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, message -> {
                    return this;
                })
                .onMessage(RacerUpdateCommand.class, message -> {
                    return this;
                })
                .build();
    }
}
