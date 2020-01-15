package util

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import model.{GenerateRequest, JsonSupport}

import scala.concurrent.duration._

trait Routes extends JsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[Routes])

  val supervisorActor: ActorRef

  implicit lazy val timeout: Timeout = Timeout(10.seconds)

  lazy val routes: Route =
    pathPrefix("generateTraffic") {
      pathEnd {
        post {
          entity(as[GenerateRequest]) { request =>
            supervisorActor ! request
            complete(HttpResponse(status = StatusCodes.OK, entity = "Request sent"))
          }
        }
      }
    }
}
