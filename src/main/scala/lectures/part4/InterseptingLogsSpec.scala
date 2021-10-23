package lectures.part4

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

// cofig actorsystem to intercept log messages
class InterseptingLogsSpec extends TestKit(ActorSystem("InterseptingLogsSpec", ConfigFactory.load().getConfig("interceptingLogMessages")))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  import InterseptingLogsSpec._

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val item = "rock"
  val creditCard = "1234-1234-1234-1234"
  val incalidCreditCard = "0000-000-000-000"

  "A checkout flow" should {
    "correctly log the dispatch of order " in {
      // occurrences = 1 - appear once
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched.", occurrences = 1) intercept {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, creditCard)
      }
    }
    "for Runtime exception" in {
      EventFilter[RuntimeException](occurrences = 1) intercept {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, incalidCreditCard)
      }
    }
  }

}

object InterseptingLogsSpec {

  // Whole folow is complicated, dificult with probes
  // There also is no conformations
  // Event filters to help.
  // Just test the last log message

  case class Checkout(item: String, creditCard: String)
  case class AuthoriseCard(card: String)
  case class DispatchOrder(item: String)
  case object PaymentAccepted
  case object PaymentDenied
  case object OrderConfirmed

  class CheckoutActor extends Actor {
    private val paumentManager = context.actorOf(Props[PaymentManagerActor])
    private val fulfillmentManager = context.actorOf(Props[FulfillmentActor])

    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fulfillmentManager ! DispatchOrder(item)
        context.become(pendingFulfilment(item))
      case PaymentDenied => throw new RuntimeException("Cant do") // for second test
    }

    def pendingFulfilment(item: String): Receive = {
      case OrderConfirmed => context.become(awaitingCheckout)
    }

   override def receive: Receive = awaitingCheckout

    def awaitingCheckout: Receive = {
      case Checkout(item, card) =>
        paumentManager ! AuthoriseCard(card)
        context.become(pendingPayment(item))
    }
  }
  class PaymentManagerActor extends Actor {
    override def receive: Receive = {
      case AuthoriseCard(card) =>
        if (card.startsWith("0")) sender() ! PaymentDenied
        else {
          Thread.sleep(4000) // possible only because application.conf has: filter-leeway = 5s
          sender() ! PaymentAccepted
        }
    }
  }
  class FulfillmentActor extends Actor with ActorLogging {
    var orderId = 43
    override def receive: Receive = {
      case DispatchOrder(item: String) =>
        orderId += 1
        log.info(s"Order $orderId for item $item has been dispatched.")
        sender() ! OrderConfirmed
    }
  }

}
