package daos

import java.sql.Timestamp
import javax.inject.Inject

import models.PowerStationEvent
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

class PowerStationEventsDao  @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {



  def insertPowerStationEvent(amount: Double, currentAmount: Double, timestamp: Long, powerStationId: Long): Future[Try[Int]] = {
    val time = new Timestamp(timestamp)
    db.run(sqlu"INSERT INTO powerstationevents(amount, ts, current_amount, power_station_id) values(${amount}, ${time}, ${currentAmount}, ${powerStationId})".asTry)
  }

  def getPowerStationEvents(powerStationId: Long): Future[Seq[PowerStationEvent]] = {
    db.run(sql"SELECT amount, UNIX_TIMESTAMP(ts) FROM powerstationevents WHERE power_station_id = ${powerStationId}"
      .as[(Double, Long)]
    ).map(v => v.map(t => PowerStationEvent(t._1, t._2)))
  }

}
