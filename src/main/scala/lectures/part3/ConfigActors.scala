package lectures.part3

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory


class TestActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case message =>
      log.info(s"My log, with my message: $message")
  }
}

object ConfigActors extends App {
  // #1  INLINE CONFIG
  // 1.
  val configString =
    """
      |akka {
      |  loglevel = "INFO"
      |}
      |""".stripMargin
  // 2.
  val config = ConfigFactory.parseString(configString)
  // 3.
  val system = ActorSystem("ConfigActorsSystem", ConfigFactory.load(config))
  val actor = system.actorOf(Props[TestActor])
  //actor ! "Hallo"

  // #2
  // CONFIG IN FILE
  // by default it load from main.resources.application.conf file
  val system2 = ActorSystem("ConfigActorsSystemFromFile")
  val actor2 = system2.actorOf(Props[TestActor])
  actor2 ! "Hallo"

  // #3
  // SEPERATE CONFIG IN SAME CONFIG FILE
  val cpecConfig = ConfigFactory.load().getConfig("specConfig")
  println(s"3. Specconfig in same fale: ${cpecConfig.getInt("top.specValue")}")

  // #4
  // SECRET CONFIG IN SECRET FILE
  val secretConfig = ConfigFactory.load("secret/secret.conf")
  println(s"4. Secret config: ${secretConfig.getString("specSecretConfig.top.password")}")

  // #5
  // DIFFERENT FILE FORMATS (json, properties)
  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"5. Json config: ${jsonConfig.getInt("top.value")}")

  // 5.1 props files dont have nested option. Fully qualified names only
  val propsConfig = ConfigFactory.load("props/propsConfig.properties")
  println(s"5.1. Properties config: ${propsConfig.getString("my.simple.property")}")
}
