package exerises.part3

import akka.actor.{ActorSystem, Actor, Props}

case class Increment(i: Int)
case class Decrement(i: Int)
case object Print

class CounterActor extends Actor {
  var value: Int = 0
  override def receive: Receive = {
    case Increment(i) => value += i
    case Decrement(i) => value -= i
    case Print => println(value)
  }
}

object Counter extends App {
  val actorSystem = ActorSystem("actorSystemEx1")
  val actor = actorSystem.actorOf(Props[CounterActor], "counterActor")

  actor ! Increment(3)
  actor ! Decrement(1)
  Thread.sleep(500)
  actor ! Print
}
