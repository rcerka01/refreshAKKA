package lectures.part4

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.util.Random

class TimedAsertionsSpec extends TestKit(ActorSystem("TimedAsertionsSpec", ConfigFactory.load().getConfig("specialTimed"))) // coonfig needed for the last test
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  import TimedAsertionsSpec._

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A worker actor must" should {
    val workerActor = system.actorOf(Props[WorkerActor])
    "reply in timely maner" in {
      within(500.millisecond, 1.second) {
        workerActor ! "work"
        expectMsg(WorkResult(42))
      }
    }
    "reply with a reasonable cadence" in {
      within(1.second) {
        workerActor ! "workSeq"
        val results = receiveWhile[Int](max=2.second, idle=500.millisecond, messages = 10) {
          case WorkResult(result) => result
        }
        assert(results.sum > 5)
      }
    }
// this test is failing, because 300ms is set in application.conf. Probes listen that, not within block
//    "reply to a test probe in timely maner" in {
//      within(1.second ){
//        val probe = TestProbe()
//        probe.send(workerActor, "work")
//        probe.expectMsg(WorkResult(42)) // timeout of 0.3 seconds in application.conf
//      }
//    }
  }

}

object TimedAsertionsSpec {

  case class WorkResult(value: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" =>
        Thread.sleep(500)   // if commented out, it will fai as well, as too fast
        sender() ! WorkResult(42)
      case "workSeq" =>
        val r = new Random()
        for (_ <- 1 to 10) {
          Thread.sleep(50)
          sender() ! WorkResult(1)
        }
    }
  }
}
