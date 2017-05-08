package services

import daos.{PowerStationDao, PowerStationEventsDao}
import models.{CreatePowerStation, PowerStation, PowerStationEvent}
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Future

class PowerStationServiceTest extends PlaySpec with ScalaFutures {

  private val powerStationDao = mock(classOf[PowerStationDao])
  private val powerStationEventsDao = mock(classOf[PowerStationEventsDao])
  private val fixture = new DefaultPowerStationService(powerStationDao, powerStationEventsDao)

  "createPowerStation " must {
    "return the created powerstation " in {
      val userId = 15l
      val powerStationType = "ptype"
      val capacity = 12d
      when(powerStationDao.insert(userId, powerStationType, capacity, 0)).thenReturn(Future.successful(1))
      whenReady(fixture.createPowerStation(CreatePowerStation(powerStationType, capacity), userId)){ powerStation =>
        assert(powerStation.powerStationType == powerStationType)
        assert(powerStation.capacity == capacity)
      }

    }
  }

  "get power station with first events " must {
    "return none if no power station" in {
      val userId = 1
      val powerStationId = 2
      when(powerStationDao.findPowerStation(userId, powerStationId)).thenReturn(Future.successful(None))

      whenReady(fixture.getPowerStationWithFirstEvents(userId, powerStationId)){t =>
        assert(t == None)
      }
    }
    "return powerstation with events if power station found" in {
      val userId = 1
      val powerStationId = 2
      val powerStation = PowerStation(2, "ptype", 12000, 3000)
      when(powerStationDao.findPowerStation(userId, powerStationId)).thenReturn(Future.successful(Some(powerStation)))
      when(powerStationEventsDao.getPowerStationEventsWithCount(powerStationId, 0, 10))
        .thenReturn(Future.successful((Seq(PowerStationEvent(1234, System.currentTimeMillis())), 20)))

      whenReady(fixture.getPowerStationWithFirstEvents(userId, powerStationId)){t =>
       assert(t.get.events.size==1)
        assert(t.get.events.filter(_.amount==1234).nonEmpty)
        assert(t.get.id == powerStation.id)
      }
    }
  }

}
