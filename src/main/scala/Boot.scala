import actor.SupervisorActor

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.datastax.driver.core.Session
import com.typesafe.config.{Config, ConfigFactory}
import repo.RepoInit
import util.{DBConfiguration, Routes}

object Boot extends App with Routes with RepoInit{
  val httpAddress: String = "localhost"
  val port: Int = 8080

  // set up ActorSystem and other dependencies here
  implicit val config: Config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem("microservice", config)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  lazy val allRoutes: Route = routes
  val session: Session = DBConfiguration().getSession
  val supervisorActor: ActorRef = system.actorOf(SupervisorActor.props(session), "supervisorActor")
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(allRoutes, httpAddress, port)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
