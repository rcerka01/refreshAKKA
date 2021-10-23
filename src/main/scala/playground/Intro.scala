package playground

import akka.actor.ActorSystem

object Intro extends App {
  val actorSystem = ActorSystem("actorsystem1")
  println(actorSystem.name)
}
