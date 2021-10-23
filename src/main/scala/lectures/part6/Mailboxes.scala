package lectures.part6

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object Mailboxes extends App {


class SimpleActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg => log.info(msg.toString)
  }
}

// 1.
class SupportTicketPriorityMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
  PriorityGenerator {
    case message: String if message.startsWith("[P0]") => 0
    case message: String if message.startsWith("[P1]") => 1
    case message: String if message.startsWith("[P2]") => 2
    case message: String if message.startsWith("[P3]") => 3
    case _ => 4
  }
)

  // #1 ORDER
  // priority output for msg beginning with high P number - P0, P1, P2 etc. Order

  // 2. Make it known in config in application.conf
  // 3. Attach it to actor
  val system = ActorSystem("MailboxSystem", ConfigFactory.load().getConfig("mailboxesDemo"))

  val supportTicketLogger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
//  supportTicketLogger ! "[P3] nice to have"
//  supportTicketLogger ! "[P0] now"
//  supportTicketLogger ! "[P1] later"

  // #2. CONTROL AWARE MAILBOX
  // 1. mark important msg as control messages
  case object ManagmentTicket extends ControlMessage
  // 2. configure, whoo will bet mailbox
  val controlActor = system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))
//  controlActor ! "[P3] nice to have"
//  controlActor ! "[P0] now"
//  controlActor ! ManagmentTicket // always recieved first

  // #2.1
  // via config
  val alContrActor = system.actorOf(Props[SimpleActor], "altControleActor")
  alContrActor ! "[P3] nice to have"
  alContrActor ! "[P0] now"
  alContrActor ! ManagmentTicket // always recieved first


}
