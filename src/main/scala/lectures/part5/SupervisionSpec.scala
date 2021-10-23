package lectures.part5

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

// if actor fails:
// 1st will suspend all children
// 2nd sends spec msg to its parent
// 3rd parents have to decide...

class SupervisionSpec  extends TestKit(ActorSystem("supervisionSpec"))
   with ImplicitSender
   with AnyWordSpecLike
   with BeforeAndAfterAll {

  import SupervisionSpec._

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Asuperviser" should {
    "resume its child in case of minor fault" in {
      val superviser = system.actorOf(Props[Superviser])
      superviser ! Props[FussyActor]
      val child = expectMsgType[ActorRef]

      child ! "I love you"
      child ! Report
      expectMsg(3)

      child ! "s s s s s s s s s s s s s s s s s s s s s s s s s s"
      child ! Report
      expectMsg(3)
    }
    "restart its child in case of empty string" in {
      val superviser = system.actorOf(Props[Superviser])
      superviser ! Props[FussyActor]
      val child = expectMsgType[ActorRef]

      child ! "I love you"
      child ! Report
      expectMsg(3)

      // because after NullPointerException actor is restarted, according on strategy
      child ! ""
      child ! Report
      expectMsg(0)
    }
    "supervisor should terminate childin case of major distraction" in {
      val superviser = system.actorOf(Props[Superviser])
      superviser ! Props[FussyActor]
      val child = expectMsgType[ActorRef]

      watch(child)
      child ! "with lower case"
      val terminatesMsg = expectMsgType[Terminated]
      assert(terminatesMsg.actor == child)
    }
    "escalate error" in {
      val superviser = system.actorOf(Props[Superviser], "superviser")
      superviser ! Props[FussyActor]
      val child = expectMsgType[ActorRef]

      // to throw Exception
      child ! 1
      val terminatesMsg = expectMsgType[Terminated]
      assert(terminatesMsg.actor == child)

    }
  }

  "kinder superviser" should {
    "not kill children in case of restart or escalate" in {
      val superviser = system.actorOf(Props[NoDeath], "superviser")
      superviser ! Props[FussyActor]
      val child = expectMsgType[ActorRef]


      child ! "I love you"
      child ! Report
      expectMsg(3)

      child ! 1
      child ! Report
      expectMsg(0)
    }
  }

  "all for one supervisor" should {
    "apply he all-for-one strategy" in {
      val superviser = system.actorOf(Props[AllForOneSupervisor], "superviserAllForOne")
      superviser ! Props[FussyActor]
      val child = expectMsgType[ActorRef]

      superviser ! Props[FussyActor]
      val secondchild = expectMsgType[ActorRef]

      secondchild ! "I love you"
      secondchild ! Report
      expectMsg(3)

      EventFilter[NullPointerException]() intercept {
        child ! ""
      }

      Thread.sleep(500)
      secondchild ! Report
      expectMsg(0)
    }
  }
}

object SupervisionSpec {
  case object Report
  class Superviser extends Actor {
    override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _:NullPointerException => Restart
      case _:IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _:Exception => Escalate
    }
    override def receive: Receive = {
      case props: Props =>
        val child = context.actorOf(props)
        sender()! child
    }
  }

  //
  class NoDeath extends Superviser {
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {} //do nothing
  }

  //
  // In case if one children fail - all children will fail
  class AllForOneSupervisor extends Superviser {

    override val supervisorStrategy: SupervisorStrategy = AllForOneStrategy() {
      case _:NullPointerException => Restart
      case _:IllegalArgumentException => Stop
      case _:RuntimeException => Resume
      case _:Exception => Escalate
    }
  }

  class FussyActor extends Actor {
    var words: Int = 0
    override def receive: Receive = {
      case Report => sender() ! words
      case "" => throw new NullPointerException("Empty string")
      case st: String =>
        if (st.length > 20) throw new RuntimeException("Sentence too big")
        else if (!Character.isUpperCase(st(0))) throw new IllegalArgumentException("Sentence must start with uppercase")
        else words += st.split(" ").length
      case _ => throw new Exception("Accept only Strings")
    }
  }
}