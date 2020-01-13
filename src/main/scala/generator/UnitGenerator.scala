package generator

import akka.actor.{ActorRef, ActorSystem, Scheduler}
import akka.event.jul.Logger
import com.peertopark.java.geocalc.EarthCalc
import model.{CompletedStep, GenerateRouteStep, Ride}
import model.OSMConstants._

import scala.concurrent.duration._
import scala.util.Random

class UnitGenerator(supervisorActor: ActorRef, scheduler: Scheduler)(implicit system: ActorSystem) {

  import system.dispatcher

  def ride(ride: Ride): Unit = {

    ride.followingSteps match {
      case Nil => Logger("logger").info(s"Unit ${ride.unitId} completed ride")
      case _ =>
        val updatedRide = updateSteps(ride)
        val timeForNextUpdate = 1 + Random.nextDouble()
        scheduler.scheduleOnce(timeForNextUpdate seconds, supervisorActor, updatedRide)
    }
  }

  def updateSteps(ride: Ride): Ride = {

    def getElapsedTime: Double = {
      ride.remainingTime match {
        case Some(elapsedTime) => elapsedTime
        case None =>
          val time = System.currentTimeMillis() / math.pow(10, 9)
          time - ride.previousTime
      }
    }

    def getDistanceToNextPoint: Double = ride.followingSteps.headOption match {
      case None => 0
      case Some(nextStep) => EarthCalc.getDistance(ride.previousStep.location, nextStep.location)
    }

    def updateCompletedSteps(step: GenerateRouteStep, time: Double): Unit = {
      val reachTime = (time * math.pow(10, 9)).toLong
      val completedStep = CompletedStep(ride.unitId, step, reachTime)
      supervisorActor ! completedStep
    }

    def updateStepsForLargerDistance(distanceBetweenPoints: Double, elapsedTime: Double,
                                     averageSpeed: Double): Ride = {

      val nextStep = ride.followingSteps.head
      val timeToNextPoint = distanceBetweenPoints / averageSpeed
      val reachTime = ride.previousTime + timeToNextPoint

      val updatedPreviousStep = nextStep
      val updatedNextSteps = ride.followingSteps.tail
      val remainingTime = elapsedTime - reachTime
      updateCompletedSteps(updatedPreviousStep, reachTime)

      updateSteps(ride.copy(previousStep = updatedPreviousStep, followingSteps = updatedNextSteps,
        previousTime = reachTime, remainingTime = Some(remainingTime)))
    }

    def updateStepsForShorterDistance(distanceBetweenPoints: Double, elapsedTime: Double): Ride = {

      val nextStep = ride.followingSteps.head
      val updatedNextPoint = CoordinatesGenerator.generateIntermediatePoint(ride.previousStep.location,
        nextStep.location, distanceBetweenPoints)
      val updatedDistance = ride.previousStep.distance - distanceBetweenPoints
      val updatedPreviousStep = ride.previousStep.copy(location = updatedNextPoint, distance = updatedDistance)

      val reachTime = ride.previousTime + elapsedTime
      updateCompletedSteps(updatedPreviousStep, reachTime)
      ride.copy(previousStep = updatedPreviousStep, previousTime = reachTime, remainingTime = None)
    }

    def updateStepsForEqualDistance(elapsedTime: Double): Ride = {

      val reachTime = ride.previousTime + elapsedTime
      updateCompletedSteps(ride.previousStep, reachTime)
      ride.copy(previousStep = ride.followingSteps.head, followingSteps = ride.followingSteps.tail,
        previousTime = reachTime, remainingTime = None)
    }

    //time in seconds
    val elapsedTime = getElapsedTime
    val speed = (ride.previousStep.duration / ride.previousStep.distance) * Random.nextDouble() * MAX_TO_AVG_SPEED_RATIO
    val traveledDistance = speed * elapsedTime
    val distanceBetweenPoints = getDistanceToNextPoint

    if (traveledDistance > distanceBetweenPoints)
      updateStepsForLargerDistance(distanceBetweenPoints, elapsedTime, speed)
    else if (traveledDistance < distanceBetweenPoints)
      updateStepsForShorterDistance(distanceBetweenPoints, elapsedTime)
    else updateStepsForEqualDistance(elapsedTime)
  }
}
