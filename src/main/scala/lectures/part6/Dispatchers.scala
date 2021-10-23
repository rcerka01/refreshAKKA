package lectures.part6

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

// Dispatchers are in charge to handle and deliver messages inside Actors

// Dispatcher types:
// 1. Dispatcher - binds actor to thread pool
// 2. PinnedDispatcher - binds actor to thread pool with exactly one thread
// 3. CallingThreadDispatcher - make sure that all executions are on calling thread


class Counter extends Actor with ActorLogging {
  var counter = 0
  override def receive: Receive = {
    case msg =>
      counter += 1
      log.info(s"[$counter] $msg")
  }
}

object Dispatchers extends App {
  val system = ActorSystem("DispatchersDemo") ///, ConfigFactory.load().getConfig("dispatchersDemo"))

  // #1 ATTACH DISPATCHER PROGRAMMATICLY
  val actors = for (i <- 1 to 10 ) yield { system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i") }
  val r = new Random()
// uncomment
//  for (i <- 1 to 1000) {
//    actors(r.nextInt(10)) ! i
//  }

  // #1.1 from config
  val rtjvm = system.actorOf(Props[Counter], "rayactor")

  // #2. DISPATCHERS IMPLEMENT EXECUTION CONTEXT TRAIT
  class DBactor extends Actor with ActorLogging {
    // solutions for this:
    // 1. Use dedicated dispatcher:
    implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("my-dispatcher")
    // 2. Use Router

    // need this for Future. In general it is discouraged
    //implicit val executionContext: ExecutionContext = context.dispatcher
    override def receive: Receive = {
      // Future is the catch (!)
      case msg => Future {
        Thread.sleep(5000)
        log.info(s"Success $msg")
      }
    }
  }

  val dbActor = system.actorOf(Props[DBactor])
 // dbActor ! "meaning of life"

  val nonBlockingActor = system.actorOf(Props[Counter])
  for (i <- 1 to 1000) {
    dbActor ! "some message" // this is starving other actors, bad
    nonBlockingActor ! "some message"
  }
}
