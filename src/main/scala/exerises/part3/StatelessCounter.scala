package exerises.part3

import akka.actor.{ActorSystem, Actor, Props}

case class IncrementS(i: Int)
case class DecrementS(i: Int)
case object PrintS

class CounterActorS extends Actor {
  //var value: Int = 0
  override def receive: Receive = receiveWithPar(0)

  def receiveWithPar(acc: Int): Receive = {
    case IncrementS(i) => context.become(receiveWithPar(acc + i))
    case DecrementS(i) => context.become(receiveWithPar(acc - i))
    case PrintS => println(acc)
  }
}

object StatelessCounter extends App {
  val actorSystem = ActorSystem("actorSystemEx1S")
  val actor = actorSystem.actorOf(Props[CounterActorS], "counterActorS")

  actor ! IncrementS(3)
  actor ! DecrementS(1)
  actor ! PrintS
}
