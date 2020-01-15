package actor

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, DeadLetter, Props, Terminated}
import akka.http.scaladsl.{Http, HttpExt}
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.datastax.driver.core.Session
import com.typesafe.config.Config
import generator.{CoordinatesGenerator, UnitGenerator}
import model._
import osm.OSMRepo
import repo.{Repo, RepoInit}
import util.AbstractDBActor

class SupervisorActor(session: Session)(implicit config: Config) extends AbstractDBActor with RepoInit {

  implicit val http: HttpExt = Http(context.system)
  implicit lazy val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val ps: PreparedStatements = initializePreparedStatements(session)
  private val repo: Repo = new Repo(session, ps, false)
  private val osmRepo: OSMRepo = new OSMRepo()

  private val unitGenerator: UnitGenerator = new UnitGenerator(self.actorRef)

  protected val osmActor: ActorRef = context.actorOf(OSMActor
    .props(repo, osmRepo, unitGenerator), "osmActor")

  protected val kafkaActor: ActorRef = context.actorOf(KafkaActor.props())

  implicit val timeout: Timeout = Timeout(10, TimeUnit.SECONDS)

  override def preStart(): Unit = {
    //    TraceLogger.debug("preStart: openOrdersSupervisor")
    context.watch(osmActor)
  }

  def receive: Receive = {

    case request: GenerateRequest => sendRequestsToGenerateRoutes(request)

    case ride: Ride => osmActor forward ride

    case completedStep: CompletedStep => kafkaActor forward completedStep

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

    val routeEnds: List[(Coordinate, Coordinate)] =
      CoordinatesGenerator.generateRandomCoordinatePairs(request.center, request.radius, request.numberOfUnits)

    routeEnds.foreach(routeEnds => osmActor ! RouteRequest(routeEnds))
  }
}

object SupervisorActor {

  def props(session: Session)(implicit config: Config): Props = Props(new SupervisorActor(session))
}
