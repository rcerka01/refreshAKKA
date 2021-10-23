package exerises.part3

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import exerises.part3.VotingAgregator.{AgregateVotes, VoteStatusReplay, VoteStatusRequest, PrintResults}

object Citizen {
  case class Vote(citizen: String)
}
class Citizen extends Actor {
  import Citizen._
  import VotingAgregator._
  override def receive: Receive = receiveVote(None)

  def receiveVote(candidate: Option[String]): Receive = {
    case Vote(c) =>
      if (candidate.isEmpty) context.become(receiveVote(Some(c)))
      else println(s"You already woted for candidate: $candidate")
    case VoteStatusRequest => sender() ! VoteStatusReplay(candidate)
  }
}

object VotingAgregator {
  case object VoteStatusRequest
  case object PrintResults
  case class VoteStatusReplay(candidate: Option[String])
  case class AgregateVotes(citizens: Set[ActorRef])
}
class VotingAgregator extends Actor {

  def addElemOption(e: Option[String], acc: Map[String, Int]): Map[String, Int] = {
    val r = if (e.isDefined)
      if (acc.isDefinedAt(e.get)) (acc filter (_._1 != e.get)) + (e.get -> (acc(e.get) + 1))
      else acc + (e.get -> 1)
    else acc
     println("Result: " + r)
    r
  }

  def receiveWithPar(el: Option[String], acc: Map[String, Int]): Receive = {
    case VoteStatusReplay(e) => context.become(receiveWithPar(e, addElemOption(e, acc)))
    case AgregateVotes(s) =>
      s foreach( r => r ! VoteStatusRequest)
  }

  override def receive: Receive = receiveWithPar(None, Map())
}

object StatelessVoting extends App {
  import Citizen._
  import VotingAgregator._

  val system = ActorSystem("votingSystem")
  val ray = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val jen = system.actorOf(Props[Citizen])
  val jim = system.actorOf(Props[Citizen])
  val jery = system.actorOf(Props[Citizen]) // not voted

  val voteAgregator = system.actorOf(Props[VotingAgregator])

  ray ! Vote("Jan")
  ray ! Vote("Maria")

  bob ! Vote("Maria")
  jen ! Vote("Alexander")
  jim ! Vote("Jan")

  voteAgregator ! AgregateVotes(Set(ray, bob, jen, jim))
}
