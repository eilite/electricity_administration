package services

import javax.inject.Inject

import daos.PowerStationDao
import models.{CreatePowerStation, PowerStation, PowerStationEvent}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

class PowerStationService @Inject() (powerStationDao: PowerStationDao) {

  def createPowerStation(createPowerStation: CreatePowerStation, userId: Long): Future[PowerStation] = {
    powerStationDao.insert(userId, createPowerStation.powerStationType, createPowerStation.capacity, 0)
      .map(powerStationId =>PowerStation(powerStationId, createPowerStation.powerStationType, createPowerStation.capacity, 0))
  }

  def loadPowerStation(userId: Long, powerStationId: Long, powerStationEvent: PowerStationEvent): Future[Try[Int]] = {
    powerStationDao.loadPowerStation(userId, powerStationId, powerStationEvent.amount)
    .flatMap {
      case Success(currentAmount: Double)=>
        powerStationDao
          .insertPowerStationEvent(amount = powerStationEvent.amount, currentAmount = currentAmount, timestamp = powerStationEvent.timestamp, powerStationId)
      case Failure(e) => Future.successful(Failure[Int](e))
    }
  }

  def consumePowerStation(userId: Long, powerStationId: Long, powerStationEvent: PowerStationEvent): Future[Try[Int]] = {
    powerStationDao.consumeFromPowerStation(userId, powerStationId, powerStationEvent.amount)
    .flatMap {
      case Success(currentAmount: Double)=>
        powerStationDao
          .insertPowerStationEvent(amount = - powerStationEvent.amount, currentAmount = currentAmount, timestamp = powerStationEvent.timestamp, powerStationId)
      case Failure(e) => Future.successful(Failure[Int](e))
    }
  }

}