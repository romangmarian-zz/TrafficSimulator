package actor

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import kafka.StepsProducer
import model.CompletedStep
import model.OSMConstants.KAFKA_TOPIC

class KafkaActor(implicit config: Config) extends Actor with ActorLogging with StepsProducer {

  override val appConfig: Config = config

  def receive: Receive = {
    case completedStep: CompletedStep =>
      Logger("log").info(self.path.name + " --->  " + completedStep.toString)
      submitMsg(KAFKA_TOPIC, completedStep)

  }
}

object KafkaActor{
  def props()(implicit config: Config): Props = Props(new KafkaActor())
}
