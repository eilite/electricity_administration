package daos

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
  private def updatePowerStation(newStock: Double, powerStationId: Long) = sqlu"UPDATE powerstations set stock = ${newStock} WHERE id = ${powerStationId}"

  def insert(userId: Long, powerStationType: String, capacity: Double, stock: Double): Future[Int] = {
    val composedAction = for {
      _ <- sqlu"INSERT INTO powerstations(user_id, ptype, capacity, stock) VALUES(${userId}, ${powerStationType}, ${capacity}, ${stock})"
      lastInsertId <- sql"SELECT LAST_INSERT_ID()".as[Int].head
    } yield lastInsertId

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
    *
    * @param userId
    * @param powerStationId
    * @param amount
    * @return the powerstation's current amount
    */
  def loadPowerStation(userId: Long, powerStationId: Long, amount: Double): Future[Try[Double]] = {
    def tryUpdatePowerStation(vect: Vector[(Long, Double, Double, String)], amount: Double): DBIOAction[Double, NoStream, Effect] = {
      if(vect.size != 1) DBIO.failed(PowerStationNotFoundException(powerStationId))
      else {
        val currentStock = vect.head._3
        val capacity = vect.head._2
        if (currentStock + amount > capacity) {
          DBIO.failed(new TooLargeAmountException)
        } else {
          updatePowerStation(currentStock + amount, powerStationId)
            .asTry
            .map(_ => currentStock + amount)
        }
      }
    }

    val composedAction = for {
      powerStations <- getPowerStation(userId, powerStationId)
      currentAmount <- tryUpdatePowerStation(powerStations, amount)
    } yield currentAmount
    db.run(composedAction.transactionally.asTry)

  }

  /**
    *
    * @param userId
    * @param powerStationId
    * @param amount
    * @return the powerstation's current electricity amount
    */
  def consumeFromPowerStation(userId: Long, powerStationId: Long, amount: Double): Future[Try[Double]] = {
    def tryUpdatePowerStation(vect: Vector[(Long, Double, Double, String)], amount: Double): DBIOAction[Double, NoStream, Effect] = {
      if (vect.size != 1) DBIO.failed(PowerStationNotFoundException(powerStationId))
      else {
        val currentStock = vect.head._3
        if (currentStock - amount < 0) {
          DBIO.failed(new TooLargeAmountException)
        } else {
          updatePowerStation(currentStock - amount, powerStationId)
            .asTry
            .map(_ => currentStock - amount)
        }
      }
    }
    val composedAction = for {
      powerStations <- getPowerStation(userId, powerStationId)
      currentAmount <- tryUpdatePowerStation(powerStations, amount)
    } yield currentAmount
    db.run(composedAction.transactionally.asTry)
  }

  def findPowerStation(userId: Long, powerStationId: Long): Future[Option[PowerStation]] = {
    db.run(getPowerStation(userId, powerStationId))
    .map(v => {
      if (v.size != 1) None
      else Some(PowerStation(powerStationId, v.head._4, v.head._2, v.head._3))
    })
  }

}
