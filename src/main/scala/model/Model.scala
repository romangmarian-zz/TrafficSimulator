package model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.peertopark.java.geocalc.Point
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TypeDefinitions {

  type OSMRouteEnds = (Coordinate, Coordinate)
}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val coordinateJsonFormat: RootJsonFormat[Coordinate] = jsonFormat2(Coordinate)
  implicit val laneJsonFormat: RootJsonFormat[Lane] = jsonFormat2(Lane)
  implicit val intersectionJsonFormat: RootJsonFormat[Intersection] = jsonFormat6(Intersection)
  implicit val maneuverJsonFormat: RootJsonFormat[Maneuver] =
    jsonFormat(Maneuver, "bearing_after", "bearing_before", "location",
      "modifier", "type", "exit")
  implicit val stepJsonFormat: RootJsonFormat[Step] = jsonFormat12(Step)
  implicit val legJsonFormat: RootJsonFormat[Legs] = jsonFormat5(Legs)
  implicit val waypointJsonFormat: RootJsonFormat[Waypoint] = jsonFormat4(Waypoint)
  implicit val routeJsonFormat: RootJsonFormat[Route] = jsonFormat6(Route)
  implicit val osmResponseJsonFormat: RootJsonFormat[OSMResponse] = jsonFormat3(OSMResponse)
  implicit val osmRequestJsonFormat: RootJsonFormat[GenerateRequest] = jsonFormat3(GenerateRequest)
}

// distance measured in meters, duration in seconds
case class PreparedStatements()

case class Coordinate(
                       latitude: Double,
                       longitude: Double
                     )

case class RouteRequest(
                         coordinates: (Coordinate, Coordinate)
                       )

case class Lane(
                 indications: List[String],
                 valid: Boolean
               )

case class Intersection(
                         bearings: Option[List[Int]],
                         lanes: Option[List[Lane]],
                         out: Option[Int],
                         in: Option[Int],
                         entry: Option[List[Boolean]],
                         location: Option[List[Double]]
                       )

case class Maneuver(
                     bearing_after: Int,
                     bearing_before: Int,
                     location: List[Double],
                     modifier: Option[String],
                     maneuverType: String,
                     exit: Option[Int]
                   )

case class Step(
                 intersections: Option[List[Intersection]],
                 geometry: String,
                 mode: String,
                 maneuver: Maneuver,
                 weight: Int,
                 duration: Int,
                 name: String,
                 distance: Int,
                 ref: Option[String],
                 rotary_name: Option[String],
                 destinations: Option[String],
                 exits: Option[String]
               ) {

  def toPoint: Point = {

    val location = maneuver.location
    Point.build(location.head, location.tail.head)
  }
}

case class Legs(
                 summary: String,
                 weight: Double,
                 duration: Double,
                 steps: Option[List[Step]],
                 distance: Double
               )

case class Waypoint(
                     hint: String,
                     distance: Double,
                     name: String,
                     location: List[Double]
                   )

case class Route(
                  geometry: String,
                  legs: Option[List[Legs]],
                  weight_name: String,
                  weight: Double,
                  duration: Double,
                  distance: Double
                )

case class OSMResponse(
                        routes: List[Route],
                        waypoints: Option[List[Waypoint]],
                        code: Option[String]
                      )

case class GenerateRoute(
                          steps: List[GenerateRouteStep]
                        )

case class GenerateRouteStep(
                              location: Point,
                              bearing: Double,
                              distance: Double,
                              duration: Double
                            )

case class CompletedStep(
                          unitId: Int,
                          step: GenerateRouteStep,
                          time: Long
                        )

case class GenerateRequest(
                            center: Coordinate,
                            radius: Double,
                            numberOfUnits: Int
                          )

case class Ride(
                 previousStep: GenerateRouteStep,
                 followingSteps: List[GenerateRouteStep],
                 previousTime: Double,
                 unitId: Int,
                 remainingTime: Option[Double] = None
               )