package lectures.part7

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import lectures.part7.TheAskSpec.AuthManager.{AUTH_FAILURE_INCORRECT_PASSWORD, AUTH_FAILURE_NOT_FOUND, AUTH_FAILURE_SYSTEM}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TheAskSpec extends TestKit(ActorSystem("theask")) with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {
  import TheAskSpec._
  import AuthManager._
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "an authenticater" should {
    authManagerTestSuite(Props[AuthManager])
  }
  "a piped authenticater" should {
    authManagerTestSuite(Props[PipedAuthManager])
  }

  def authManagerTestSuite(props: Props) = {
    "fail authenticate unregistered user" in {
      val authManager = system.actorOf(props)
      authManager ! Authenticate("some","some")
      expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))
    }
    "fail authenticate invalid password" in {
      val authManager = system.actorOf(props)
      authManager ! RegUser("some", "some")
      authManager ! Authenticate("some","other")
      expectMsg(AuthFailure(AUTH_FAILURE_INCORRECT_PASSWORD))
    }
    "authenticate user" in {
      val authManager = system.actorOf(props)
      authManager ! RegUser("some", "some")
      authManager ! Authenticate("some","some")
      expectMsg(AuthSuccess)
    }
  }

}

object TheAskSpec {

  case class Read(key: String)
  case class Write(key: String, v: String)

  class KVactor extends Actor with ActorLogging {
    override def receive: Receive = online(Map())

    def online(kv:  Map[String, String]): Receive = {
      case Read(k) =>
        log.info(s"Reading Option ${k}")
        sender() ! kv.get(k)
      case Write(k, v) =>
        log.info(s"Writing $k with $v")
        context.become(online(kv + (k -> v)))
    }
  }

  case class RegUser(u: String, p: String)
  case class Authenticate(u: String, p: String)
  case class AuthFailure(msg: String)
  case object AuthSuccess

  object AuthManager {
    val AUTH_FAILURE_NOT_FOUND = "Username not found"
    val AUTH_FAILURE_INCORRECT_PASSWORD = "Incorrect password"
    val AUTH_FAILURE_SYSTEM = "System failure"
  }

  class AuthManager extends Actor with ActorLogging {
    import AuthManager._
    // for ?
    implicit val timeout = Timeout(1.second)
    implicit  val executeContext: ExecutionContext = context.dispatcher

    protected val authDb =  context.actorOf(Props[KVactor])
    override def receive: Receive = {
      case RegUser(u, p) => authDb ! Write(u,p)
      case Authenticate(u, p) => authenticate(u,p)
    }

    def authenticate(u: String, p: String) = {
      val futureAuth = (authDb ? Read(u))
      val originalSender = sender() // never call sender() in onCompleate (within diff thread)
      futureAuth.onComplete {
        case Success(None) => originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Success(Some(pw)) =>
          if (pw == p) originalSender ! AuthSuccess
          else originalSender ! AuthFailure(AUTH_FAILURE_INCORRECT_PASSWORD)
        case Failure(_) => originalSender ! AuthFailure(AUTH_FAILURE_SYSTEM)
      }
    }
  }

  // same thing, but nicer
  class PipedAuthManager extends AuthManager {
    override def authenticate(u: String, p: String): Unit = {
      val futureAuth = (authDb ? Read(u))
      val originalSender = sender() // never call sender() in onCompleate (within diff thread)

      val mapedFutureAuth = futureAuth.mapTo[Option[String]] // Future[Any] -> Future[Option[String]]

      val responseFuture = mapedFutureAuth map {
        case None => originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
        case Some(pw) =>
          if (pw == p) originalSender ! AuthSuccess
          else originalSender ! AuthFailure(AUTH_FAILURE_INCORRECT_PASSWORD)
      }

      responseFuture pipeTo sender() // (!) Avoid system brake etc..
    }
  }
}
