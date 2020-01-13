package actor

import akka.actor.{Actor, ActorLogging}
import akka.event.jul.Logger
import model.CompletedStep

class KafkaActor extends Actor with ActorLogging{

  def receive: Receive = {
    case completedStep: CompletedStep => Logger("logger").info(completedStep.toString)
  }
}
