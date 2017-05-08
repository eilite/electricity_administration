package services

import javax.inject.Inject

import daos.{PowerStationDao, PowerStationEventsDao}
import models.{CreatePowerStation, PowerStation, PowerStationWithEvents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefaultPowerStationService @Inject()(powerStationDao: PowerStationDao, powerStationEventsDao: PowerStationEventsDao) extends PowerStationService {

  def createPowerStation(createPowerStation: CreatePowerStation, userId: Long): Future[PowerStation] = {
    powerStationDao.insert(userId, createPowerStation.powerStationType, createPowerStation.capacity, 0)
      .map(powerStationId =>PowerStation(powerStationId, createPowerStation.powerStationType, createPowerStation.capacity, 0))
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