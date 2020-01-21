package kafka

import akka.actor.{Actor, ActorRef}
import cakesolutions.kafka.akka.{KafkaProducerActor, ProducerRecords}
import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import com.typesafe.config.Config
import model.CompletedStep
import org.apache.kafka.common.serialization.StringSerializer

trait StepsProducer {
  this: Actor =>

  import CompletedStepProtocol._

  def appConfig: Config

  lazy val kafkaProducerConf = KafkaProducer.Conf(
    bootstrapServers = appConfig.getString("kafka.bootstrap.servers"),
    keySerializer = new StringSerializer,
    valueSerializer = new JsonSerializer[CompletedStep]
  )

  lazy val kafkaProducerActor: ActorRef = context.actorOf(KafkaProducerActor.props( kafkaProducerConf))

  def submitMsg(topic: String, completedStep: CompletedStep): Unit = {
    kafkaProducerActor ! ProducerRecords(List(KafkaProducerRecord(topic, completedStep.unitId, completedStep)))
  }
}
