package lectures.part3

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

// HIERARCHY
// / -the root guardian
// /system - system actors
// /user - programmer created actors

// NEVER
// never pass a mutable actor state, or "this" to call methods directly
// it also brake async locks etc.

object Parent {
  case class CreateChild(name: String)
  case class MessageToChild(msg: String)
}

class Parent extends Actor {
  import Parent._
  override def receive: Receive = {
    case CreateChild(n) => {
      println("Child creation")
      val childRef = context.actorOf(Props[Child], n)  // can create Child actor out of context
      context.become(wiithChild(childRef))
    }
  }

  def wiithChild(ref: ActorRef): Receive = {
    case MessageToChild(msg) => ref forward msg
  }
}

class Child extends Actor {

  override def receive: Receive = {
    case msg => println(s"${context.self.path} got message: $msg")
  }
}

object ChildActors extends  App {
  import Parent._

  val system = ActorSystem("childSystem")
  val actorParent = system.actorOf(Props[Parent], "parentActor")
  actorParent ! CreateChild("myChild")
 // actorParent ! CreateChild("myAnotherChild") this will break
  actorParent ! MessageToChild("Hallo child")

  // CHILD BY PATH
  val child = system.actorSelection("/user/parentActor/myChild")
  child ! MessageToChild("Found you")
}
