package services

import javax.inject.Inject

import daos.{PowerStationDao, PowerStationEventsDao}
import models.{CreatePowerStation, PowerStation, PowerStationEvent, PowerStationWithEvents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class PowerStationService @Inject() (powerStationDao: PowerStationDao, powerStationEventsDao: PowerStationEventsDao) {

  def createPowerStation(createPowerStation: CreatePowerStation, userId: Long): Future[PowerStation] = {
    powerStationDao.insert(userId, createPowerStation.powerStationType, createPowerStation.capacity, 0)
      .map(powerStationId =>PowerStation(powerStationId, createPowerStation.powerStationType, createPowerStation.capacity, 0))
  }

  def loadPowerStation(userId: Long, powerStationId: Long, powerStationEvent: PowerStationEvent): Future[Try[Int]] = {
    powerStationDao.loadPowerStation(userId, powerStationId, powerStationEvent.amount)
    .flatMap {
      case Success(currentAmount: Double)=>
        powerStationEventsDao
          .insertPowerStationEvent(powerStationEvent.amount, currentAmount, powerStationEvent.timestamp, powerStationId)
      case Failure(e) => Future.successful(Failure[Int](e))
    }
  }

  def consumePowerStation(userId: Long, powerStationId: Long, powerStationEvent: PowerStationEvent): Future[Try[Int]] = {
    powerStationDao.consumeFromPowerStation(userId, powerStationId, powerStationEvent.amount)
    .flatMap {
      case Success(currentAmount: Double)=>
        powerStationEventsDao
          .insertPowerStationEvent(- powerStationEvent.amount, currentAmount, powerStationEvent.timestamp, powerStationId)
      case Failure(e) => Future.successful(Failure[Int](e))
    }
  }

  def getPowerStationWithFirstEvents(userId: Long, powerStationId: Long): Future[Option[PowerStationWithEvents]] = {
    powerStationDao.findPowerStation(userId, powerStationId).flatMap {
      case Some(powerStation: PowerStation) => {
        powerStationEventsDao.getPowerStationEventsWithCount(powerStation.id, 0, 10)
          .map(t => Some(PowerStationWithEvents(powerStation, t._1)))
      }
      case None => Future.successful(None)
    }
  }

}