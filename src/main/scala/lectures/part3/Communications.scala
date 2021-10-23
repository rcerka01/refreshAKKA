package lectures.part3

import akka.actor.{Actor, ActorRef, ActorSystem, Props}


case class TestClass(n: String)

object Communications extends App {
  // THIS ALL HAPPEN ASYNCHRONOUSLY!!!

  class SimpleActor extends Actor {
    // 2.
    context
    context.self // eqvivalent to "this". It incluses path, name and identifier. eg
    self // same as context.self

    override def receive: Receive = {
      case "Hi 3" => context.sender() ! "Hallo there" // bob reply to alise. sender() keep reference of last sent message of actor
      case s: String => println(s)
      case n: Int => println(n)
      case t: TestClass => println(t.n) // Usually use case class or object. Passing object must be SERIALIZABLE and IMUTABLE (obv)
                                        // SERIALIZABLE means transferable to bytecode
      // 2.
      case m: MessageToMyself =>
        self ! m.m  // this will send to same, Simple actor message, and will consume it as string (1st option)
      // 3.
      case r: SayHiTo => r.ref ! "Hi 3"
      // 5.
      case f: ForwardingClass => f.r forward f.c
      case _ =>
    }
  }

  val actorSystem = ActorSystem("actorsystem2")
  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "Hi"
  simpleActor ! 3
  simpleActor ! TestClass("zuba")

  // 2.
  // Actors has information about context and them selfs e.g. Actor[akka://actorsystem2/user/simpleActor#2145490630]
  case class MessageToMyself(m: String)
  simpleActor ! MessageToMyself("Message to myself")

  // 3.
  // reply
  case class SayHiTo(ref: ActorRef)
  val alise = actorSystem.actorOf(Props[SimpleActor], "alise")
  val bob = actorSystem.actorOf(Props[SimpleActor], "bob")
  alise ! SayHiTo(bob)

  // 4.
  // If there is no sender e.g. reply to initial, default, null sender, then message s go to dead letters (garbige pool)

  // 5.
  // forward messages
  case class ForwardingClass(c: String, r: ActorRef)
  alise ! ForwardingClass("forwarded message", bob)
}
