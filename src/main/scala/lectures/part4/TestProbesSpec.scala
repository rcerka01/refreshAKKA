package lectures.part4

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class TestProbesSpec extends TestKit(ActorSystem("TestProbesSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  import TestProbesSpec._

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The master Actor" should {
    val master = system.actorOf(Props[Master])
    "register slaves" in {
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
    }
    "send work to slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workLoad = "love shit"
      master ! Work(workLoad)

      // interaction master to slave
      slave.expectMsg(SlaveWork(workLoad, testActor))
      slave.reply(WorkCompleated(2, testActor))

      expectMsg(Report(2))
    }

    "agregate data correctly" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workLoad = "love shit"
      master ! Work(workLoad)
      master ! Work(workLoad)

      slave.receiveWhile() {
        case SlaveWork(`workLoad`, `testActor`) => slave.reply(WorkCompleated(2, testActor))
      }

      expectMsg(Report(2))
      expectMsg(Report(4))
    }
  }

}

object TestProbesSpec {
  case class Work(txt: String)
  case class SlaveWork(txt: String, ref: ActorRef)
  case class WorkCompleated(count: Int, ref: ActorRef)
  case class Register(ref: ActorRef)
  case class Report(total: Int)
  case object RegistrationAck

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegistrationAck
        context.become(online(slaveRef, 0))
      case _ =>
    }

    def online(slaveRef: ActorRef, totalCount: Int): Receive = {
      case Work(t) => slaveRef ! SlaveWork(t, sender())
      case WorkCompleated(c, origRequester) =>
        val total = totalCount + c
        origRequester ! Report(total)
        context.become(online(slaveRef, total))
    }
  }
}
