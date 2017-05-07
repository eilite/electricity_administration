package daos

import java.sql.Timestamp
import javax.inject.Inject

import exceptions.{PowerStationNotFoundException, TooLargeAmountException}
import models.PowerStation
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class PowerStationDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  private def getPowerStation(userId: Long, powerStationId: Long) = sql"SELECT id, capacity, stock, ptype FROM powerstations WHERE user_id = ${userId} AND id = ${powerStationId}".as[(Long, Double, Double, String)]
  private def updatePowerStationStock(newStock: Double, powerStationId: Long) = sqlu"UPDATE powerstations set stock = ${newStock} WHERE id = ${powerStationId}"
  private def insertPowerStationEvent( amount: Double, timestamp: Long, currentAmount: Double, powerStationId: Long) = {
    val time = new Timestamp(timestamp)
    sqlu"INSERT INTO powerstationevents(amount, ts, current_amount, power_station_id) values(${amount}, ${time}, ${currentAmount}, ${powerStationId})"
  }

  def insert(userId: Long, powerStationType: String, capacity: Double, stock: Double): Future[Int] = {
    val composedAction = for {
    _ <- sqlu"INSERT INTO powerstations(user_id, ptype, capacity, stock, created) VALUES(${userId}, ${powerStationType}, ${capacity}, ${stock}, NOW())"
    lastInsertId <- sql"SELECT LAST_INSERT_ID()".as[Int].head
    } yield lastInsertId

    db.run(composedAction.transactionally)
  }

  def findPowerStation(userId: Long, powerStationId: Long): Future[Option[PowerStation]] = {
    db.run(getPowerStation(userId, powerStationId))
      .map(v => {
        if (v.size != 1) None
        else Some(PowerStation(powerStationId, v.head._4, v.head._2, v.head._3))
      })
  }

  def update(userId: Long, powerStationId: Long, powerStationType: String, capacity: Double): Future[Try[PowerStation]] = {
    val composedAction = for {
      numberOfUpdate <- sqlu"UPDATE powerstations SET ptype = ${powerStationType}, capacity = ${capacity} WHERE id = ${powerStationId} AND user_id = ${userId}"
      powerStationTuple <- if(numberOfUpdate == 0) DBIO.failed(new PowerStationNotFoundException(powerStationId)) else getPowerStation(userId, powerStationId).head
    } yield PowerStation(powerStationId, powerStationTuple._4, powerStationTuple._2, powerStationTuple._3)
    db.run(composedAction.transactionally.asTry)
  }

  def delete(id: Long, userId: Long): Future[Int] = {
    db.run(sqlu"DELETE FROM powerstations WHERE id = ${id} and user_id = ${userId}")
  }


  def getPowerStations(userId: Long): Future[Seq[PowerStation]] = {
    db.run(sql"SELECT id, ptype, capacity, stock FROM powerstations WHERE user_id = ${userId} ORDER BY created DESC".as[(Long, String, Double, Double)])
      .map(v => v.map(t => new PowerStation(t._1, t._2, t._3, t._4)))
  }


  def loadPowerStation(userId: Long, powerStationId: Long, amount: Double, timestamp: Long): Future[Try[Int]] = {
    def tryUpdatePowerStation(vect: Vector[(Long, Double, Double, String)], amount: Double): DBIOAction[Double, NoStream, Effect] = {
      if(vect.size != 1) DBIO.failed(PowerStationNotFoundException(powerStationId))
      else {
        val currentStock = vect.head._3
        val capacity = vect.head._2
        if (currentStock + amount > capacity) {
          DBIO.failed(new TooLargeAmountException)
        } else {
          updatePowerStationStock(currentStock + amount, powerStationId)
            .asTry
            .map(_ => currentStock + amount)
        }
      }
    }

    val composedAction = for {
    powerStations <- getPowerStation(userId, powerStationId)
    currentAmount <- tryUpdatePowerStation(powerStations, amount)
    i <- insertPowerStationEvent(amount, timestamp, currentAmount, powerStationId)
    } yield i
    db.run(composedAction.transactionally.asTry)
  }

  def consumeFromPowerStation(userId: Long, powerStationId: Long, amount: Double, timestamp: Long): Future[Try[Int]] = {
    def updatePowerStation(vect: Vector[(Long, Double, Double, String)], amount: Double): DBIOAction[Double, NoStream, Effect] = {
      if (vect.size != 1) DBIO.failed(PowerStationNotFoundException(powerStationId))
      else {
        val currentStock = vect.head._3
        if (currentStock - amount < 0) {
          DBIO.failed(new TooLargeAmountException)
        } else {
          updatePowerStationStock(currentStock - amount, powerStationId)
            .asTry
            .map(_ => currentStock - amount)
        }
      }
    }

    val composedAction = for {
    powerStations <- getPowerStation(userId, powerStationId)
    currentAmount <- updatePowerStation(powerStations, amount)
    i <- insertPowerStationEvent(- amount, timestamp, currentAmount, powerStationId)
    } yield i
    db.run(composedAction.transactionally.asTry)
  }


}
