package actor

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, DeadLetter, Props, Terminated}
import akka.http.scaladsl.{Http, HttpExt}
import akka.pattern._
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.Config
import generator.{CoordinatesGenerator, UnitGenerator}
import model._
import osm.OSMRepo
import util.AbstractDBActor

class SupervisorActor()(implicit config: Config) extends AbstractDBActor {

  implicit val http: HttpExt = Http(context.system)
  implicit lazy val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val osmRepo: OSMRepo = new OSMRepo()

  private val unitGenerator: UnitGenerator = new UnitGenerator(self.actorRef)

  protected val osmRouter: ActorRef =
    context.actorOf(FromConfig.props(OSMActor.props(osmRepo, unitGenerator)), "osmRouter")
  protected val kafkaRouter: ActorRef =
    context.actorOf(FromConfig.props(KafkaActor.props()), "kafkaRouter")

  implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)

  override def preStart(): Unit = {
    context.watch(osmRouter)
    context.watch(kafkaRouter)
  }

  def receive: Receive = {

    case request: GenerateRequest => sendRequestsToGenerateRoutes(request)

    case ride: Ride => osmRouter forward ride

    case completedStep: CompletedStep => kafkaRouter forward completedStep

    case e: DeadLetter =>
      log.warning(s"The ${e.recipient} is not able to process message: ${e.message}")

    case t: Terminated =>
      log.error(s"Stopping actor and shutting down system because of actor: ${t.actor.path}")
      context.stop(self)
      context.system.terminate

    case _ =>
      log.error("Message is not processed")
  }

  private def  sendRequestsToGenerateRoutes(request: GenerateRequest): Unit =  {

    val routeEndsList: List[(Coordinate, Coordinate)] =
      CoordinatesGenerator.generateRandomCoordinatePairs(request.center, request.radius, request.numberOfUnits)

    routeEndsList.foreach(routeEnds => {
      //sleep pt a nu suprasolicita serverul OSRM
      Thread.sleep(2)
      osmRouter ! RouteRequest(routeEnds)
    })
  }
}

object SupervisorActor {

  def props()(implicit config: Config): Props = Props(new SupervisorActor())
}
