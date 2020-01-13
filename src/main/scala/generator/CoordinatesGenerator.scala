package generator

import model.{Coordinate, GenerateRoute, GenerateRouteStep, OSMResponse, Step, TypeDefinitions}

import math.{Pi, cos, sin, sqrt}
import scala.util.Random
import com.peertopark.java.geocalc._

object CoordinatesGenerator extends TypeDefinitions {

  def generateRandomCoordinatePairs(origin: Coordinate, radius: Double, nbOfPairs: Int): List[OSMRouteEnds] = {

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

  def toGenerateRoute(response: OSMResponse): GenerateRoute = {

    val selectedRoute = response.routes.head
    val steps = selectedRoute.legs.getOrElse(Nil).flatMap(_.steps.getOrElse(Nil))

    val generateRouteSteps = steps.map(toGenerateRouteStep)
    GenerateRoute(generateRouteSteps)
  }

  private def toGenerateRouteStep(step: Step): GenerateRouteStep = {

    val location = step.toPoint
    val bearing = step.maneuver.bearing_after

    GenerateRouteStep(location, bearing, step.distance, step.duration)
  }
}
