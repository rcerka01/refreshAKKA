package lectures.part6

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, FromConfig, RoundRobinGroup, RoundRobinPool, RoundRobinRoutingLogic, Router, Broadcast}
import com.typesafe.config.ConfigFactory

// LOGICS AVAILABLE:
// round-robin
// random
// smallest mailbox
// broadcast
// scatter-gather-first
// tail-chopping
// consistent-hashing

object Routers extends App {

  // #1 Manual router
  // 1.
  class Master extends Actor with ActorLogging {
    private val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave_$i")
      context.watch(slave)
      // TODO
      ActorRefRoutee(slave)
    }

    // 2. Def router
    private val router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      // 4. Termination (lifecycle). Replice if slave dies
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router.addRoutee(newSlave)
      // 3. Route msge   // it routes now without involving master
      case msg => router.route(msg, sender())
    }
  }


  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  val system = ActorSystem("routerSystem", ConfigFactory.load().getConfig("routersDemo"))
  val master = system.actorOf(Props[Master])
  // shall create new 10 slaves in round robin logic

// UNCOMENT FOR #1
//  for (i <- 1 to 10 ) {
//    master ! s"[$i] Haallo"
//  }

  // #2 POOL ROUTHER
  val poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]),"SimplePoolMaster")
  // baslicly does the same thing as the first one
//      for (i <- 1 to 10 ) {
//      poolMaster ! s"[$i] Haallo"
//    }

  // #2.1
  // FROM CONFIGURATION file
  val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMaster2")
//  for (i <- 1 to 10 ) {
//    poolMaster2 ! s"[$i] Haallo"
//  }

  // #3 GROUP ROUTER
  // group router around existing slaves
  val slaveList = ((1 to 5 ) map (i => system.actorOf(Props[Slave], s"slave_$i"))).toList
  val slavePaths = slaveList.map (r => r.path.toString)
  val groupMaster = system.actorOf(RoundRobinGroup(slavePaths).props())
//  for (i <- 1 to 10 ) {
//    groupMaster ! s"[$i] Haallo"
//  }

  // #3.1
  // FROM CONFIGURATION file
  val groupMaster2 = system.actorOf(FromConfig.props(), "groupMaster2")
//  for (i <- 1 to 10 ) {
//    groupMaster2 ! s"[$i] Haallo"
//  }

  // SPECIAL MESSAGES
  // sends to all (5) child
  groupMaster2 ! Broadcast("Hallo everyone")

  // PoisonPill and Kill here NOT apply
  // AddRoutee, Remove, Get, handled by routing actor
}
