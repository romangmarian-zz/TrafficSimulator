package generator

import akka.actor.{ActorRef, ActorSystem}
import akka.event.jul.Logger
import com.peertopark.java.geocalc.EarthCalc
import model.OSMConstants._
import model.{CompletedStep, Coordinate, Ride}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class UnitGenerator(supervisorActor: ActorRef)(implicit system: ActorSystem) {

  import system.dispatcher

  def ride(ride: Ride): Unit = {

    updateCompletedSteps(ride)
    ride.followingSteps match {
      case Nil =>
        Logger("logger").info(s"Unit ${ride.unitId} completed ride")
      case _ =>
        val updatedRide = updateSteps(ride)
        val timeForNextUpdate = 1 + Random.nextDouble()
        system.scheduler.scheduleOnce(timeForNextUpdate seconds, supervisorActor, updatedRide)
    }
  }

  def updateCompletedSteps(ride: Ride): Unit = {
    val reachTime = (ride.previousTime * math.pow(10, 9)).toLong
    val completedStep = CompletedStep(ride.unitId, Coordinate(ride.previousStep.location.getLatitude,
      ride.previousStep.location.getLongitude), reachTime)
    supervisorActor ! completedStep
  }

  def updateSteps(ride: Ride): Ride = {

    def getElapsedTime: Double = {
      ride.remainingTime match {
        case Some(elapsedTime) => elapsedTime
        case None =>
          val time = System.currentTimeMillis() / math.pow(10, 3)
          time - ride.previousTime
      }
    }

    def getDistanceToNextPoint: Double =
      EarthCalc.getDistance(ride.previousStep.location, ride.followingSteps.head.location)

    def updateStepsForLargerDistance(distanceBetweenPoints: Double, elapsedTime: Double,
                                     averageSpeed: Double): Ride = {

      val nextStep = ride.followingSteps.head
      val timeToNextPoint = distanceBetweenPoints / averageSpeed
      val reachTime = ride.previousTime + timeToNextPoint

      val updatedPreviousStep = nextStep
      val updatedNextSteps = ride.followingSteps.tail
      val remainingTime = elapsedTime - timeToNextPoint

      ride.copy(previousStep = updatedPreviousStep, followingSteps = updatedNextSteps, previousTime = reachTime,
        remainingTime = Some(remainingTime))
    }

    def updateStepsForShorterDistance(traveledDistance: Double, distanceBetweenPoints: Double,
                                      elapsedTime: Double): Ride = {

      val nextStep = ride.followingSteps.head
      val intermediatePoint = CoordinatesGenerator.generateIntermediatePoint(ride.previousStep.location,
        nextStep.location, traveledDistance)
      val updatedDistance = distanceBetweenPoints - traveledDistance
      val updatedPreviousStep = ride.previousStep.copy(location = intermediatePoint, distance = updatedDistance)

      val reachTime = ride.previousTime + elapsedTime
      ride.copy(previousStep = updatedPreviousStep, previousTime = reachTime, remainingTime = None)
    }

    def updateStepsForEqualDistance(elapsedTime: Double): Ride = {

      val reachTime = ride.previousTime + elapsedTime
      ride.copy(previousStep = ride.followingSteps.head, followingSteps = ride.followingSteps.tail,
        previousTime = reachTime, remainingTime = None)
    }

    //time in seconds
    val elapsedTime = getElapsedTime
    val speed = (ride.previousStep.distance / ride.previousStep.duration) * Random.nextDouble() * MAX_TO_AVG_SPEED_RATIO
    val traveledDistance = speed * elapsedTime
    val distanceBetweenPoints = getDistanceToNextPoint

    if (traveledDistance > distanceBetweenPoints)
      updateStepsForLargerDistance(distanceBetweenPoints, elapsedTime, speed)
    else if (traveledDistance < distanceBetweenPoints)
      updateStepsForShorterDistance(traveledDistance, distanceBetweenPoints, elapsedTime)
    else updateStepsForEqualDistance(elapsedTime)
  }
}
