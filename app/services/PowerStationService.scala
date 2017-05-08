package services

import com.google.inject.ImplementedBy
import models.{CreatePowerStation, PowerStation, PowerStationWithEvents}

import scala.concurrent.Future
@ImplementedBy(classOf[DefaultPowerStationService])
trait PowerStationService {
  def createPowerStation(createPowerStation: CreatePowerStation, userId: Long): Future[PowerStation]

  def getPowerStationWithFirstEvents(userId: Long, powerStationId: Long): Future[Option[PowerStationWithEvents]]
}
