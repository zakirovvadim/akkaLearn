package org.example;

import akka.actor.typed.ActorSystem;

public class Main {
//        public static void main(String[] args) {
//        ActorSystem<String> actorSystem = ActorSystem.create(FirstSimpleBehavior.create(), "FirstActorSystem");
//        actorSystem.tell("say hello");
//        actorSystem.tell("who are you");
//        actorSystem.tell("create a child");
//        actorSystem.tell("start");
//    }
    public static void main(String[] args) {
        ActorSystem<ManagerBehavior.Command> manager = ActorSystem.create(ManagerBehavior.create(), "Manager");
        manager.tell(new ManagerBehavior.InstructionCommand("start"));
    }
}
