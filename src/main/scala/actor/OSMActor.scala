package actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import com.softwaremill.id.IdGenerator
import model._
import osm.OSMRepo
import repo.Repo

class OSMActor(repo: Repo, osmRepo: OSMRepo, idGenerator: IdGenerator) extends Actor with ActorLogging {

  import context.dispatcher

  def receive: Receive = {
    case request: RouteRequest =>
      osmRepo.getOSMRoute(request.coordinates) pipeTo sender
  }

  def handleRouteRequest(request: RouteRequest): Unit = {

    val osmResponse = osmRepo.getOSMRoute(request.coordinates)

  }
}

object OSMActor {
  def props(repo: Repo, osmRepo: OSMRepo, idGenerator: IdGenerator): Props =
    Props(new OSMActor(repo, osmRepo, idGenerator))
}
