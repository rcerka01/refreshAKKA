package lectures.part4

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestProbe}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.duration._

class SychronousTestSpec extends AnyWordSpecLike with BeforeAndAfterAll {
  import SychronousTestSpec._

  // IMPLICIT IMPORTANT
  implicit val systemSync: ActorSystem = ActorSystem("synchronousTestSpec")

  override def afterAll(): Unit = {
    systemSync.terminate()
  }

  "A counter" should {
    "synchronously increase its counter" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter ! Inc // counter already recieved mesage
      assert(counter.underlyingActor.count == 1)
    }
    "synchronously increase its counter at the call of the rceive function" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter.receive(Inc)
      // same thing
      assert(counter.underlyingActor.count == 1)
    }
    "work on calling thread dispatcher" in {
      val actor = systemSync.actorOf(Props[Counter].withDispatcher(CallingThreadDispatcher.Id))
      val probe = TestProbe()
      probe.send(actor, Read)
      probe.expectMsg(Duration.Zero, 0) // how to make sure value is already received
    }
  }
}

object SychronousTestSpec {
  case object Inc
  case object Read

  class Counter extends Actor {
    var count = 0
    override def receive: Receive = {
      case Inc => count += 1
      case Read => sender() ! count
    }
  }
}
