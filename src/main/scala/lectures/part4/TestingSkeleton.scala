package lectures.part4

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class TestingSkeleton extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The thing being tested" should {
    "do this " in {
      // scenario
    }
  }

}

object TestingSkeleton {}
