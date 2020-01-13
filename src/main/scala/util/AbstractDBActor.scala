package util

import java.util.ConcurrentModificationException

import akka.actor.SupervisorStrategy.{Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, DeadLetter, OneForOneStrategy}
import akka.pattern.AskTimeoutException
import com.datastax.driver.core.exceptions.{NoHostAvailableException, QueryExecutionException, QueryValidationException, UnsupportedFeatureException}
import com.typesafe.config.Config

import scala.concurrent.TimeoutException

abstract class AbstractDBActor extends Actor with ActorLogging {
  implicit val config: Config = context.system.settings.config

  context.system.eventStream.subscribe(self, classOf[DeadLetter])

  override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
    case ufe: UnsupportedFeatureException =>
      ufe.printStackTrace()
      Stop

    case qve: QueryValidationException =>
      qve.printStackTrace()
      Stop

    case qee: QueryExecutionException =>
      qee.printStackTrace()
      Resume

    case cme: ConcurrentModificationException =>
      cme.printStackTrace()
      Restart

    case ate: AskTimeoutException =>
      ate.printStackTrace()
      Resume

    case te: TimeoutException =>
      Restart

    case nhe: NoHostAvailableException =>
      nhe.printStackTrace()
      Resume

    case ex: Exception =>
      ex.printStackTrace()
      Resume
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    super.postRestart(reason)
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self)
  }
}

