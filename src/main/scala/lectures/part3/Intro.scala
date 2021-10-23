package lectures.part3

import akka.actor.{Actor, ActorSystem, Props}

object Intro extends App {

  // 1. ActorSystem
  // Define actor system. One per project
  val actorSystem = ActorSystem("actorsystem")
  println(actorSystem.name)

  // 2. Actor
  // to count words
  class WordCounter extends Actor {
    var counter = 0

    override def receive: PartialFunction[Any, Unit] = { // Receive is just alias of PF
      case s: String => {
        counter += s.split(" ").length
        println(counter)
      }
      case unknown => // in case any other type - do nothing
    }
  }

  // 3. Instantiate Actor
  val actInst = actorSystem.actorOf(Props[WordCounter], "wordCounter")
  val actInst2 = actorSystem.actorOf(Props[WordCounter], "wordCounter2")
  // both work asynchronously!!

  // 4. Comunicate
  actInst ! "some new day"
  actInst2 ! "new day"


  ///////////////////////// PASS VARIABLES TO ACTOR
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => print(s"Hi, my name is $name")
    }
  }

  // via companion object (better)
  object Person {
    def props(name: String) = Props(new Person(name))
  }

  //val person = actorSystem.actorOf(Props(new Person("Bob")))
  val person = actorSystem.actorOf(Person.props("Bob")) // is same but better
  person ! "hi"
}
