package daos

import com.google.inject.ImplementedBy
import models.PowerStation

import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[DefaultPowerStationDao])
trait PowerStationDao {
  def insert(userId: Long, powerStationType: String, capacity: Double, stock: Double): Future[Int]

  def findPowerStation(userId: Long, powerStationId: Long): Future[Option[PowerStation]]

  def update(userId: Long, powerStationId: Long, powerStationType: String, capacity: Double): Future[Try[PowerStation]]

  def delete(id: Long, userId: Long): Future[Int]

  def getPowerStations(userId: Long): Future[Seq[PowerStation]]

  def loadPowerStation(userId: Long, powerStationId: Long, amount: Double, timestamp: Long): Future[Try[Int]]

  def consumeFromPowerStation(userId: Long, powerStationId: Long, amount: Double, timestamp: Long): Future[Try[Int]]
}
