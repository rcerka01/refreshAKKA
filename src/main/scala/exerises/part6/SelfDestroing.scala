package exerises.part6

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, PoisonPill, Props}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

// SEEMS IMPLEMENTED CORRECTLY, BUT NOT WORK :)
// SAME AS EXAMPLE
// BETTER WAY TO DO TIMERS IS WITH TIMERS TRAIT

object SelfDestroing extends App {
  implicit val system = ActorSystem("SelfDistrSystem")
  implicit val executionContext: ExecutionContext = system.dispatcher

  class SelfDestroingActor extends Actor with ActorLogging {

    def countDown(): Cancellable = system.scheduler.scheduleOnce(1.second) {
      self ! PoisonPill
    }

    override def receive: Receive = {
      case msg =>
        log.info(s"Got $msg")
        countDown().cancel
        countDown()
    }
  }

  val actor = system.actorOf(Props[SelfDestroingActor])

  system.scheduler.schedule(800.millisecond, 800.millisecond) {
    actor ! "zibzib1"
  }
}
