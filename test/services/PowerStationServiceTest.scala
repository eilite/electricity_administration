package services

import daos.{PowerStationDao, PowerStationEventsDao}
import exceptions.PowerStationNotFoundException
import models.{CreatePowerStation, PowerStation, PowerStationEvent, PowerStationWithEvents}
import org.mockito.Mockito
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PowerStationServiceTest extends PlaySpec with ScalaFutures {

  private val powerStationDao = mock(classOf[PowerStationDao])
  private val powerStationEventsDao = mock(classOf[PowerStationEventsDao])
  private val fixture = new PowerStationService(powerStationDao, powerStationEventsDao)

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

  "load power station " must {
    "return failure if user does not have access to powerstation" in {
      val userId = 1
      val powerStationId = 2
      val powerStationEvent = PowerStationEvent(100, System.currentTimeMillis)

      when(powerStationDao.loadPowerStation(userId, powerStationId, 100)).thenReturn(Future.successful(Failure(new PowerStationNotFoundException(powerStationId))))

      whenReady(fixture.loadPowerStation(userId, powerStationId, powerStationEvent)){ t =>
        assert(t.isFailure)
        assert(t.failed.get.isInstanceOf[PowerStationNotFoundException])
      }
    }

    "store event if conditions fulfilled" in {
      val userId = 1
      val powerStationId = 2
      val powerStationEvent = PowerStationEvent(100, System.currentTimeMillis)

      when(powerStationDao.loadPowerStation(userId, powerStationId, 100)).thenReturn(Future.successful(Success(340.5)))
      when(powerStationEventsDao.insertPowerStationEvent(100, 340.5, powerStationEvent.timestamp, powerStationId)).thenReturn(Future.successful(Success(1)))
      whenReady(fixture.loadPowerStation(userId, powerStationId, powerStationEvent)){ _ =>
        Mockito.verify(powerStationEventsDao).insertPowerStationEvent(100, 340.5, powerStationEvent.timestamp, powerStationId)
      }
    }
  }

  "consume power station " must {
    "return failure if user does not have access to powerstation" in {
      val userId = 1
      val powerStationId = 2
      val powerStationEvent = PowerStationEvent(100, System.currentTimeMillis)

      when(powerStationDao.consumeFromPowerStation(userId, powerStationId, 100)).thenReturn(Future.successful(Failure(new PowerStationNotFoundException(powerStationId))))

      whenReady(fixture.consumePowerStation(userId, powerStationId, powerStationEvent)){ t =>
        assert(t.isFailure)
        assert(t.failed.get.isInstanceOf[PowerStationNotFoundException])
      }
    }

    "store event if conditions fulfilled" in {
      val userId = 1
      val powerStationId = 2
      val powerStationEvent = PowerStationEvent(100, System.currentTimeMillis)

      when(powerStationDao.consumeFromPowerStation(userId, powerStationId, 100)).thenReturn(Future.successful(Success(340.5)))
      when(powerStationEventsDao.insertPowerStationEvent(-100, 340.5, powerStationEvent.timestamp, powerStationId)).thenReturn(Future.successful(Success(1)))
      whenReady(fixture.consumePowerStation(userId, powerStationId, powerStationEvent)){ _ =>
        Mockito.verify(powerStationEventsDao).insertPowerStationEvent(-100, 340.5, powerStationEvent.timestamp, powerStationId)
      }
    }
  }

  "get power station with first events " must {
    "return none if no power station" in {
      val userId = 1
      val powerStationId = 2
      when(powerStationDao.getPowerStation(userId, powerStationId)).thenReturn(Future.successful(None))

      whenReady(fixture.getPowerStationWithFirstEvents(userId, powerStationId)){t =>
        assert(t == None)
      }
    }
    "return powerstation with events if power station found" in {
      val userId = 1
      val powerStationId = 2
      val powerStation = PowerStation(2, "ptype", 12000, 3000)
      when(powerStationDao.getPowerStation(userId, powerStationId)).thenReturn(Future.successful(Some(powerStation)))
      when(powerStationEventsDao.getPowerStationEventsWithCount(powerStationId, 0, 10))
        .thenReturn(Future.successful((Seq(PowerStationEvent(1234, System.currentTimeMillis())), 20)))

      whenReady(fixture.getPowerStationWithFirstEvents(userId, powerStationId)){t =>
       assert(t.get.events.size==1)
        assert(t.get.id == powerStation.id)
      }
    }
  }

}
