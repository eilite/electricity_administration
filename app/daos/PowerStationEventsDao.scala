package daos

import com.google.inject.ImplementedBy
import models.PowerStationEvent

import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[DefaultPowerStationEventsDao])
trait PowerStationEventsDao {

  def insertPowerStationEvent(amount: Double, currentAmount: Double, timestamp: Long, powerStationId: Long): Future[Try[Int]]

  def getPowerStationEventsWithCount(powerStationId: Long, offset: Int, limit: Int): Future[(Seq[PowerStationEvent], Int)]
}
