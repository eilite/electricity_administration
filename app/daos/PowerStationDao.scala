package daos

import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class PowerStationDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {


  def insert(userId: Long, powerStationType: String, capacity: Double):Future[Int] = {
    val composedAction = (sqlu"INSERT INTO powerstations(user_id, ptype, capacity) VALUES(${userId}, ${powerStationType}, ${capacity})"
      .flatMap(_ => sql"SELECT LAST_INSERT_ID()".as[Int].head))
    db.run(composedAction.transactionally)
  }

  def delete(id: Long, userId: Long): Future[Int] = {
    db.run(sqlu"DELETE FROM powerstations WHERE id = ${id} and user_id = ${userId}")
  }



}
