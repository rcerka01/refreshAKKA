package lectures.part5

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}

// ACTOR CAN BE:
// started - new ActorRef and id (UUID)
// suspended
// resumed
// restarted - internal state destroyed
// stopped

object StartChild
class lifeCycleActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case StartChild => context.actorOf(Props[lifeCycleActor], "child")
  }

  override def preStart(): Unit = log.info(s"I am started")
  override def postStop(): Unit = log.info("I am stopped")
}

object Lifecycle extends App {

  // # PRE POST START HOOKS
  val system = ActorSystem("LifeCycleActorSystem")
//  val parent = system.actorOf(Props[lifeCycleActor], "paren")
//  parent ! StartChild
//  // child stops first(!!!)
//  parent ! PoisonPill

  // # RESTART
  case object Fail
  case object FailChaild
  case object CheckChaild
  case object Check


  class Parent extends Actor {
    private val child = system.actorOf(Props[Child], "supervisedChild")
    override def receive: Receive = {
      case FailChaild => child ! Fail
      case CheckChaild => child ! Check
    }
  }

  class Child extends Actor with ActorLogging {
    override def preStart(): Unit = log.info("Supervised started")
    override def postStop(): Unit = log.info("Supervised stopped")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.info(s"Supervised child restarting ${reason.getMessage}")
    override def postRestart(reason: Throwable): Unit = log.info(s"Supervised child restarted ${reason.getMessage}")

    override def receive: Receive = {
      case Fail =>
        log.warning("Chaild will fail now")
        throw new RuntimeException("I failed")
      case Check => log.info("I am alive")
    }
  }

  val superviser = system.actorOf(Props[Parent], "superevisor")
  // AS A RESULT CHILD IS RESTARTED
  superviser ! FailChaild
  // conformation
  superviser ! CheckChaild

  // it is because of DEFAULT SUPERVISION STRATEGY
}
