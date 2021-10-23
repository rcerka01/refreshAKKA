package lectures.part5

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}


object Parent {
  case class StartChild(name: String)
  case class StopChild(name: String)
  case object Stop
}

class Parent extends Actor with ActorLogging {
  import Parent._

  override def receive: Receive = withChildren(Map())

  def withChildren(ch: Map[String,  ActorRef]): Receive = {
    case StartChild(name) => {
      log.info(s"Starting child $name")
      context.become(withChildren(ch + (name -> context.actorOf(Props[Child], name))))
    }
    case StopChild(name) => {
      log.info(s"Stopping child $name")
      val childOption = ch.get(name)
      childOption.foreach(ref => context.stop(ref))
    }
    case Stop => {
      log.info("Stopping myself")
      // THIS STOPS ALSO ALL CHILD ACTORS
      context.stop(self)
    }
    case msg => log.info(msg.toString)
  }
}

class Child extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg => log.info(s"[Child] $msg")
  }
}

object StartStopActors extends App {
  import Parent._
  val system = ActorSystem("StartStopActors")
  val parent = system.actorOf(Props[Parent], "parent")
  parent ! StartChild("child1")
  val child1 = system.actorSelection("/user/parent/child1")
  child1 ! "hallo child1"

  // #1 CONTEXT STOP
  // STOPPING IS ASYNCHRONOUS PROCESS
  parent ! StopChild("child1")

  // some messages still go through after stopping
  //for (_ <- 1 to 50) child1 ! "Still there?"

  parent ! StartChild("child2")
  val child2 = system.actorSelection("/user/parent/child2")
  child1 ! "hallo child2"

  // this will stop all child (child2)
  parent ! Stop

  // #2 BY USING SPECIAL MESSAGES
  val poisonChild = system.actorOf(Props[Child], "toPoison")
  poisonChild ! PoisonPill
  poisonChild !  "Poisoned child, still there?"

  // this will throw Kill exception
  val killedChild = system.actorOf(Props[Child], "toKill")
  killedChild ! Kill
  killedChild !  "Killedchild, still there?"

  // #3
  // DEATH WATCH
  class DethWatcher extends Actor with ActorLogging {
    override def receive: Receive = {
      case StartChild(n) =>
        val c = context.actorOf(Props[Child], n)
        log.info(s"Starting to watch childs $n death:" )
        context.watch(c)
      case Terminated(r) =>
        log.info(s"Reference watching: $r is stopped")
    }
  }

  val watcher = system.actorOf(Props[DethWatcher], "watcher")
  watcher ! StartChild("watchedCgild")
  val wChild = system.actorSelection("/user/watcher/watchedCgild")
  Thread.sleep(1000)

  wChild ! PoisonPill
}
