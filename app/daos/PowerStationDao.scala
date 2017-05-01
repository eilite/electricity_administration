package daos

import java.sql.Timestamp
import javax.inject.Inject

import exceptions.{PowerStationNotFoundException, TooLargeAmountException}
import models.{PowerStation, PowerStationEvent}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class PowerStationDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  private def getPowerStation(userId: Long, powerStationId: Long) = sql"SELECT id, capacity, stock FROM powerstations WHERE user_id = ${userId} AND id = ${powerStationId}".as[(Long, Double, Double)]
  private def updatePowerStation(newStock: Double, powerStationId: Long) = sqlu"UPDATE powerstations set stock = ${newStock} WHERE id = ${powerStationId}"

  def insert(userId: Long, powerStationType: String, capacity: Double, stock: Double): Future[Int] = {
    val composedAction = (sqlu"INSERT INTO powerstations(user_id, ptype, capacity, stock) VALUES(${userId}, ${powerStationType}, ${capacity}, ${stock})"
      .flatMap(_ => sql"SELECT LAST_INSERT_ID()".as[Int].head))
    db.run(composedAction.transactionally)
  }

  def delete(id: Long, userId: Long): Future[Int] = {
    db.run(sqlu"DELETE FROM powerstations WHERE id = ${id} and user_id = ${userId}")
  }


  def getPowerStations(userId: Long): Future[Seq[PowerStation]] = {
    db.run(sql"SELECT id, ptype, capacity, stock FROM powerstations WHERE user_id = ${userId}".as[(Long, String, Double, Double)])
    .map(v => v.map(t => new PowerStation(t._1, t._2, t._3, t._4)))
  }

  /**
    * gets powerstation and loads it if required conditions fulfilled
    * @param userId
    * @param powerStationId
    * @param amount
    * @return the powerstation's current amount
    */
  def loadPowerStation(userId: Long, powerStationId: Long, amount: Double): Future[Try[Double]] = {
    db.run(
      getPowerStation(userId, powerStationId).flatMap(v => {
        if(v.size != 1) DBIO.failed(PowerStationNotFoundException(powerStationId))
        else {
          val currentStock = v.head._3
          val capacity = v.head._2
          if (currentStock + amount > capacity) {
            DBIO.failed(new TooLargeAmountException)
          } else {
            updatePowerStation(currentStock + amount, powerStationId)
              .asTry
              .map(_ => currentStock + amount)
          }
        }
      }).transactionally.asTry
    )
  }

  def consumeFromPowerStation(userId: Long, powerStationId: Long, amount: Double): Future[Try[Double]] = {
    db.run(
      getPowerStation(userId, powerStationId).flatMap(v =>{
        if (v.size != 1) DBIO.failed(PowerStationNotFoundException(powerStationId))
        else {
          val currentStock = v.head._3
          if (currentStock - amount < 0) {
            DBIO.failed(new TooLargeAmountException)
          } else {
            updatePowerStation(currentStock - amount, powerStationId)
              .asTry
              .map(_ => currentStock - amount)
          }
        }
      }).transactionally.asTry
    )
  }

  def insertPowerStationEvent(amount: Double, currentAmount: Double, timestamp: Long, powerStationId: Long): Future[Try[Int]] = {
    val time = new Timestamp(timestamp)
    db.run(sqlu"INSERT INTO powerstationevents(amount, ts, current_amount, power_station_id) values(${amount}, ${time}, ${currentAmount}, ${powerStationId})".asTry)
  }

  def getPowerStationEvents(userId: Long, powerStationId: Long): Future[Seq[PowerStationEvent]] = {
    db.run(sql"SELECT amount, ts FROM powerstationevents pse, powerstations ps WHERE ps.user_id = ${userId} AND ps.id = pse.power_station_id = ${powerStationId}"
      .as[(Double, Long)]
    )
    .map(v => v.map(t => PowerStationEvent(t._1, t._2)))
  }


}
