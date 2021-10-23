package exerises.part3

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

// check Github for proper solution and pattern to work with childs
// https://github.com/rockthejvm/akka-essentials/blob/master/src/main/scala/part2actors/ChildActorsExercise.scala

object WordCounterMaster {
  case class Initialize(i: Int)
  case class WordCountTask(txt: String)
  case class WordCountReplay(i: Int)
}

class WordCounterMaster extends Actor {
  import WordCounterMaster._

  def aux(current: Int, total: Int): Int =
    if (current < total - 1) current
    else 0

  def recieveWithParams(list: List[ActorRef], current: Int, total: Int): Receive = {
    case WordCountTask(txt) => {
      list(current) ! WordCountTask(txt)
      context.become(recieveWithParams(list, aux(current + 1, total), total))
    }
    case WordCountReplay(i) => {
      println(s"Replay recieved: $i")
    }
  }

  override def receive: Receive = {
    case Initialize(n) => {
      val list = List.fill(n)(context.actorOf(Props[WordCounterWorker]))
      context.become(recieveWithParams(list, 0, n))
    }
  }
}

class WordCounterWorker extends Actor {
  import WordCounterMaster._
  override def receive: Receive = {
    case WordCountTask(s) => {
      println(s" $self receive: $s")
      sender() ! WordCountReplay(s.split(" ").length)
    }
  }
}


object WordCounts extends App {
  import WordCounterMaster._

  val system = ActorSystem("WordCounterSystem")
  val master = system.actorOf(Props[WordCounterMaster], "masterActor")
  master ! Initialize(3)
  master ! WordCountTask("We like France")
  master ! WordCountTask("We like Greece and")
  master ! WordCountTask("We like Italy one more")
  master ! WordCountTask("We like Spain day")
  master ! WordCountTask("We like")
  master ! WordCountTask("We like")
  master ! WordCountTask("We like")
}
