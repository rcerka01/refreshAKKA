package lectures.part3


import akka.actor.{Actor, ActorSystem, Props, ActorLogging}
import akka.event.Logging

// DEBUG
// INFO
// WARNING / WARN
// ERROR

// Logging is asynchronous

class ActorLogging1 extends Actor {
  // #1 Explicit Logging
  val logger = Logging(context.system, this)
  override def receive: Receive = {
    case message =>
      logger.info(s"Some Loggs 1, plus: $message")
  }
}

// #2 By adding Logging trait (most popular)
class ActorLogging2 extends Actor with ActorLogging {
  override def receive: Receive = {
    // String interpolators
    case (a, b) => log.info("Recieved {} and {}", a, b)
    case message =>
      log.info(s"Some Loggs 2, plus: $message")
  }
}

object LoggingActors extends App {
  val system = ActorSystem("loggingActorSystem")
  val actor = system.actorOf(Props[ActorLogging2])

  actor ! "Some message"
  actor ! (2,3)
}
