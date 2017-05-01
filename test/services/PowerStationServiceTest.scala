package services

import daos.PowerStationDao
import models.CreatePowerStation
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class PowerStationServiceTest extends PlaySpec with ScalaFutures {

  private val powerStationDao = mock(classOf[PowerStationDao])
  private val fixture = new PowerStationService(powerStationDao)

  "createPowerStation " must {
    " must return the created powerstation " in {
      val userId = 15l
      val powerStationType = "ptype"
      val capacity = 12d
      when(powerStationDao.insert(userId, powerStationType, capacity)).thenReturn(Future.successful(1))
      whenReady(fixture.createPowerStation(CreatePowerStation(powerStationType, capacity), userId)){ powerStation =>
        assert(powerStation.powerStationType == powerStationType)
        assert(powerStation.capacity == capacity)
      }

    }
  }
}
