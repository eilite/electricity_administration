package services

import javax.inject.Inject

import daos.PowerStationDao
import models.{CreatePowerStation, PowerStation}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PowerStationService @Inject() (powerStationDao: PowerStationDao) {

  def createPowerStation(createPowerStation: CreatePowerStation, userId: Long): Future[PowerStation] = {
    powerStationDao.insert(userId, createPowerStation.powerStationType, createPowerStation.capacity)
      .map(powerStationId =>PowerStation(powerStationId, createPowerStation.powerStationType, createPowerStation.capacity))
  }
}
