package lectures.part4

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import lectures.part4.BasicSpec.{BlackHoleActor, LabTestActor, SimpleActor}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.util.Random

class BesicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The Simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "Hallo"
      echoActor ! message
      expectMsg(message)
    }
  }

  // fails in 3 sec. Configurable: akka.test.single-expect-default
  // testActor is sender for all tests. Passed in implicitly
  "The Black Hole actor" should {
    "send back the same message" in {
      val blackHallActor = system.actorOf(Props[BlackHoleActor])
      val message = "Hallo"
      blackHallActor ! message
      expectNoMessage(1.second)
    }
  }

  // assertations
  "The Lab Test ctor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "sturn string into upercase" in {
      val message = "Hallo"
      labTestActor ! message
      val reply = expectMsgType[String] // that give all string abilties to manipulate
      assert(reply === "HALLO")
    }
    "reply to random (ether) greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("Hi", "Hello")
    }
    "reply to favourite tech" in {
      labTestActor ! "favourite"
      expectMsgAllOf("Scala", "Akka")
    }
    "reply in different way to fav tech" in {
      labTestActor ! "favourite"
      val messages = receiveN(2)
    }
    "reply in cooler way to fav tech" in {
      labTestActor ! "favourite"
      expectMsgPF() {
        case "Scala" => //only care if PF is defined
        case "Akka" =>
      }
    }
  }

}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }
  class BlackHoleActor extends Actor {
    override def receive: Receive = {
      case message =>
    }
  }
  class LabTestActor extends Actor {
    val random = new Random
    override def receive: Receive = {
      case "greeting" =>
        if (random.nextBoolean()) sender() ! "Hi" else sender() ! "Hello"
      case "favourite" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => message.toUpperCase()
    }
  }
}
