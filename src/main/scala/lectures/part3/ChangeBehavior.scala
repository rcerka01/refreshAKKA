package lectures.part3

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object FussyKid {
  val HAPPY = "happy"
  val UNHAPPY = "unhappy"
  case object Accept
  case object REject
}

class FussyKid extends Actor {
  import Mother._
  import FussyKid._
  var state = HAPPY
  override def receive: Receive = {
    case Food(VEGETABLES) => state = UNHAPPY
    case Food(FRUIT) => state = HAPPY
    case Message(msg) =>
      if (state == HAPPY) sender() ! Accept
      else sender() ! REject
  }
}

// GET RID OF THE STATE VARIABLE
class StatelessFussyKid extends Actor {
  import Mother._
  import FussyKid._
  override def receive: Receive = happyState // default

  // context.become(unhappyState) is default for context.become(unhappyState, true)
  // if false, the behavior become as a Stuck
  // to wipe off context.unbecome(unhappyState)
  def happyState: Receive = {
    case Food(VEGETABLES) => context.become(unhappyState) // toggle
    case Food(FRUIT) => // stay happy
    case Message(msg) => sender() ! Accept
  }

  def unhappyState: Receive = {
    case Food(VEGETABLES) => // stay unhappy
    case Food(FRUIT) => context.become(happyState) // toggle
    case Message(msg) => sender() ! REject
  }
}

object Mother {
  case class Start(kidReference: ActorRef)
  case class Food(f: String)
  case class Message(msg: String)
  val VEGETABLES = "vegetables"
  val FRUIT = "fruit"
}

class Mother extends Actor {
  import Mother._
  import FussyKid._
  override def receive: Receive = {
    case Start(r) =>
      r ! Food(VEGETABLES)
      r ! Message("Happy now?")
    case Accept => println("Nice")
    case REject => println("Fuck Off then")
  }
}

object ChangeBehavior extends App {
  import Mother._
  import FussyKid._

  val system = ActorSystem("FussyKidSystem")
  val kid = system.actorOf(Props[FussyKid])
  val stableKid = system.actorOf(Props[FussyKid])
  val mother = system.actorOf(Props[Mother])

  mother ! Start(stableKid)
}
