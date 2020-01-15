package generator

import com.peertopark.java.geocalc._
import com.softwaremill.id.{DefaultIdGenerator, IdGenerator}
import model.{Coordinate, OSMResponse, GenerateRouteStep, Step, Ride}

import scala.math.{Pi, cos, sin, sqrt}
import scala.util.Random

object CoordinatesGenerator {

  protected val idGenerator: IdGenerator = new DefaultIdGenerator()

  def generateRandomCoordinatePairs(origin: Coordinate, radius: Double,
                                    nbOfPairs: Int): List[(Coordinate, Coordinate)] = {

    1.to(nbOfPairs).map(_ =>
      (generateCoordinateInRadius(origin, radius), generateCoordinateInRadius(origin, radius))).toList
  }

  def generateCoordinateInRadius(origin: Coordinate, radius: Double): Coordinate = {
    val r = radius * sqrt(Random.nextDouble())
    val angle = Random.nextDouble() * 2 * Pi

    val x = origin.latitude + r * cos(angle)
    val y = origin.longitude + r * sin(angle)

    Coordinate(x, y)
  }

  def generateIntermediatePoint(origin: Point, destination: Point, distance: Double): Point = {

    val bearing = EarthCalc.getBearing(origin, destination)
    EarthCalc.pointRadialDistance(origin, bearing, distance)
  }

  def toGenerateRoute(response: OSMResponse): List[GenerateRouteStep] = {

    val selectedRoute = response.routes.head
    val steps = selectedRoute.legs.getOrElse(Nil).flatMap(_.steps.getOrElse(Nil))

    steps.map(toGenerateRouteStep)
  }

  private def toGenerateRouteStep(step: Step): GenerateRouteStep = {

    val location = step.toPoint
    val bearing = step.maneuver.bearing_after

    GenerateRouteStep(location, bearing, step.distance, step.duration)
  }

  def createRideStart(route: List[GenerateRouteStep]): Ride = Ride(route.head, route.tail, System.currentTimeMillis()/math.pow(10,3), idGenerator.nextId())
}
