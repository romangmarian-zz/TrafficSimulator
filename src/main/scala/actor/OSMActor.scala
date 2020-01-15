package actor

import akka.actor.{Actor, ActorLogging, ActorRef, DeadLetter, Props}
import akka.event.jul.Logger
import generator.{CoordinatesGenerator, UnitGenerator}
import model._
import osm.OSMRepo
import repo.Repo

import scala.concurrent.ExecutionContext.Implicits.global

class OSMActor(repo: Repo, osmRepo: OSMRepo, unitGenerator: UnitGenerator) extends
  Actor with ActorLogging {

  val logger = Logger("log")

  def receive: Receive = {
    case request: RouteRequest => handleRouteRequest(request, sender)
    case ride: Ride => unitGenerator.ride(ride)
    case e: DeadLetter =>
      log.warning(s"The ${e.recipient} is not able to process message: ${e.message}")
  }

  def handleRouteRequest(request: RouteRequest, sender: ActorRef): Unit = {

    val osmResponse = osmRepo.getOSMRoute(request.coordinates)
    val routeSteps = osmResponse.map { osmRoute => CoordinatesGenerator.toGenerateRoute(osmRoute) }
    val routeStart = routeSteps.map { steps => CoordinatesGenerator.createRideStart(steps) }
    routeStart.map(routeStart => sender ! routeStart)
  }
}

object OSMActor {
  def props(repo: Repo, osmRepo: OSMRepo, unitGenerator: UnitGenerator): Props =
    Props(new OSMActor(repo, osmRepo, unitGenerator))
}
