package exerises.part3

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

case class Withdraw(amount: Double, player: ActorRef)
case class Deposit(amount: Double, player: ActorRef)
object Statement

object Context {
  val actorSystem: ActorSystem = ActorSystem("actorSystemEx2")
}

class BankTransaction extends Actor {
  import Context._

  var account: Double = 0
  val bankConformationActor: ActorRef = actorSystem.actorOf(Props[BankConformation])
  override def receive: Receive = {
    case Withdraw(a, ar) => try {
      throw new RuntimeException
      account -= a
      bankConformationActor ! Success(account, ar)
    } catch  {
      case e: Throwable => bankConformationActor ! Failure(account, ar)
    }
    case Deposit(a, ar) => try {
      account += a
      bankConformationActor ! Success(account, ar)
    } catch {
      case e: Throwable => bankConformationActor ! Failure(account, ar)
    }
    case Statement => println(f"Account balance is :$account%.2f")
    case _ =>
  }
}

case class Success(amount: Double, ar: ActorRef)
case class Failure(amount: Double, ar: ActorRef)

class BankConformation extends Actor {
  override def receive: Receive = {
    case Success(a, ar) => println(f"Transaction Successful, account balance is $a%.2f for ${ar.toString()}")
    case Failure(a, ar) => println(f"Transaction Failed, account balance remain $a%2.2f for ${ar.toString()}")
  }
}

object BankAccount extends App {
  import Context._

  val actorRay = actorSystem.actorOf(Props[BankTransaction], "actorRay")

  actorRay ! Deposit(10.20, actorRay)
  actorRay ! Withdraw(5.05, actorRay)
  actorRay ! Statement
}
