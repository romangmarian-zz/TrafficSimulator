package actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.jul.Logger
import model.CompletedStep

class KafkaActor extends Actor with ActorLogging{

  def receive: Receive = {
    case completedStep: CompletedStep => Logger("logger").info(completedStep.toString)
  }
}

object KafkaActor{
  def props(): Props = Props(new KafkaActor())
}
