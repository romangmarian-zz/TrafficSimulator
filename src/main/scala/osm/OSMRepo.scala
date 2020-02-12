package osm

import akka.event.jul.Logger
import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import model.{Coordinate, JsonSupport, OSMResponse}
import model.OSMConstants.URL

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class OSMRepo(implicit val http: HttpExt, val actorMaterializer: ActorMaterializer) extends JsonSupport {

  val logger = Logger("logger")

  def getRequestURI(coordinates: (Coordinate, Coordinate)): String = {
    val x = URL + s"${coordinates._1.longitude},${coordinates._1.latitude};" +
      s"${coordinates._2.longitude},${coordinates._2.latitude}" + "?alternatives=false&steps=true&overview=false"
    logger.warning(x)
    x
  }

  def getHTTPResponse(coordinates: (Coordinate, Coordinate)): Future[HttpResponse] = {
    http.singleRequest(HttpRequest(GET, getRequestURI(coordinates)))
  }

  def toOSMRoute(httpResponse: HttpResponse): Future[OSMResponse] = httpResponse match {
    case HttpResponse(StatusCodes.OK, _, entity, _) =>
      Unmarshal(entity).to[OSMResponse]
    case HttpResponse(status, _, entity, _) =>
      Future.failed(new Exception(handleEndpointError(entity, status)))
  }

  private def handleEndpointError(entity: ResponseEntity, status: StatusCode): String = {
    val errorMessage: String = "OSRM Error: " + Unmarshal(entity).to[String]
    logger.warning(errorMessage)
    errorMessage
  }

  def getOSMRoute(coordinates: (Coordinate, Coordinate)): Future[OSMResponse] = {
    getHTTPResponse(coordinates).flatMap(toOSMRoute)
  }
}
