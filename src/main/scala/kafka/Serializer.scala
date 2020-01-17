package kafka

import java.util

import model.{CompletedStep, Coordinate}
import org.apache.kafka.common.serialization.{Deserializer, Serializer, StringDeserializer, StringSerializer}
import play.api.libs.json.{Format, Json, Reads, Writes}

object CompletedStepProtocol {
  implicit val coordinateFormat: Format[Coordinate] = Json.format[Coordinate]
  implicit val completedStepFormat: Format[CompletedStep] = Json.format[CompletedStep]
}

class JsonDeserializer[A: Reads] extends Deserializer[A] {

  private val stringDeserializer = new StringDeserializer

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit =
    stringDeserializer.configure(configs, isKey)

  override def deserialize(topic: String, data: Array[Byte]): A =
    Json.parse(stringDeserializer.deserialize(topic, data)).as[A]

  override def close(): Unit =
    stringDeserializer.close()

}

class JsonSerializer[A: Writes] extends Serializer[A] {

  private val stringSerializer = new StringSerializer

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit =
    stringSerializer.configure(configs, isKey)

  override def serialize(topic: String, data: A): Array[Byte] =
    stringSerializer.serialize(topic, Json.stringify(Json.toJson(data)))

  override def close(): Unit =
    stringSerializer.close()

}