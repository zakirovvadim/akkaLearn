package org.example.racer;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    public interface Command extends Serializable {
    }

    public static class StartCommand implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    @AllArgsConstructor
    @Getter
    public static class RacerUpdateCommand implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
        private ActorRef<Racer.Command> racer;
        private int position;
    }

    private static class GetPositionCommand implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    private RaceController(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(RaceController::new);
    }

    private Map<ActorRef<Racer.Command>, Integer> currentPosition;
    private long start;
    private final int raceLength = 100;
    private Object TIMER_KEY;

    private void displayRace() {
        int displayLength = 160;
        for (int i = 0; i < 50; ++i) System.out.println();
        System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
        System.out.println("    " + new String(new char[displayLength]).replace('\0', '='));
        int i = 0;
        for (ActorRef<Racer.Command> racer : currentPosition.keySet()) {
            System.out.println(i + " : " + new String(new char[currentPosition.get(racer) * displayLength / 100]).replace('\0', '*'));
            i++;
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, message -> {
                    start = System.currentTimeMillis();
                    currentPosition = new HashMap<>();
                    for (int i = 0; i < 10; i++) {
                        ActorRef<Racer.Command> racer = getContext().spawn(Racer.create(), "racer" + i);
                        currentPosition.put(racer, 0);
                        racer.tell(new Racer.StartCommand(raceLength));
                    }
                    return Behaviors.withTimers(timer -> {
                        timer.startTimerAtFixedRate(TIMER_KEY, new GetPositionCommand(), Duration.ofSeconds(1));
                        return this;
                    });
                })
                .onMessage(GetPositionCommand.class, message -> {
                    for (ActorRef<Racer.Command> racer : currentPosition.keySet()) {
                        racer.tell(new Racer.PositionCommand(getContext().getSelf()));
                        displayRace();
                    }
                    return this;
                })
                .onMessage(RacerUpdateCommand.class, message -> {
                    currentPosition.put(message.getRacer(), message.getPosition());
                    return this;
                })
                .build();
    }
}
