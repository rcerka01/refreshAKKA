package lectures.part7

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

//Resource actor:
  // can be open
  // can be close

// [Read, Open, Read, Write, Close]
// in this case first message will be stashed

case object Open
case object Close
case object Read
case class Write(data: String)

// 1.
class ResourceActor extends Actor with ActorLogging with Stash {
  private var innerData: String = ""

  override def receive: Receive = closed

  def open: Receive = {
    case Read =>
      log.info(s"Read $innerData")
    case Write(data) =>
      log.info(s"Writing $data")
      innerData = data
    case Close =>
      log.info("Close")
      unstashAll()
      context.become(closed)
    case msg =>
      stash()
      log.info("Stashing in open state")

  }
  def closed: Receive = {
    case Open =>
      log.info("Open")
      // 3.
      unstashAll()
      context.become(open)
    case msg =>
      log.info(s"Stashing $msg in closed state")
      // 2.
      stash()

  }
}

object Stash extends App {

  val system = ActorSystem("StashActorSystem")
  val actor = system.actorOf(Props[ResourceActor])

  actor ! Read
  actor ! Open
  actor ! Open
  actor ! Write("love")
  actor ! Close
  actor ! Read


}
