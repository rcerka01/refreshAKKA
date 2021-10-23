package lectures.part6

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, PoisonPill, Props, Timers}

import scala.concurrent.duration._

class SimpleActorM extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg => log.info(msg.toString)
  }
}

// SCHEDULERS ARE NOT FOR MILLISECONDS OR LONG TIMES (month etc)

object TimersAndSchedulers extends App {

  val system = ActorSystem("TimersAndSchedulers")
  val simpleActor = system.actorOf(Props[SimpleActorM])

  // same as mixed in trait
  system.log.info("")

  // (!)      Scheduling must happen un certain thread. For that is needed execution context. That implements system.dispatcher
   implicit val executionConntext = system.dispatcher // Another way how to involve system dispatcher
  // import system.dispatcher                           // Another way how to involve system dispatcher
  system.scheduler.scheduleOnce(1.second){
    simpleActor ! "remainder"
  } // (system.dispatcher)

  val routine: Cancellable = system.scheduler.schedule(1.second, 2.seconds){
    simpleActor ! "hartbeat"
  }

  system.scheduler.scheduleOnce(5.seconds) {
    routine.cancel()
  }

  // TAIMERS
  case object TimerKey // can be anything. Smth like timer ID
  case object Start
  case object Remainder
  case object Stop
  class TimerActor extends Actor with ActorLogging with Timers {
    // this will send Start maessage
    // it will reset itself at every time called
    timers.startSingleTimer(TimerKey, Start, 500.milliseconds)

    override def receive: Receive = {
      case Start =>
        log.info("Start")
        timers.startTimerWithFixedDelay(TimerKey, Remainder, 2.seconds)
      case Remainder =>
        log.info("Remainder")
      case Stop =>
        log.info("Stopped")
        timers.cancel(TimerKey)
        self ! PoisonPill
      case _ =>
        log.info("Undefined")
    }
  }

  val timerActor = system.actorOf(Props[TimerActor])

// uncomment to test
//  system.scheduler.scheduleOnce(5.seconds) {
//    timerActor ! Stop
//  }
}
