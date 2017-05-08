package daos

import java.sql.Timestamp
import javax.inject.Inject

import models.PowerStationEvent
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class DefaultPowerStationEventsDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with PowerStationEventsDao {

  def insertPowerStationEvent(amount: Double, currentAmount: Double, timestamp: Long, powerStationId: Long): Future[Try[Int]] = {
    val time = new Timestamp(timestamp)
    db.run(sqlu"INSERT INTO powerstationevents(amount, ts, current_amount, power_station_id) values(${amount}, ${time}, ${currentAmount}, ${powerStationId})".asTry)
  }

  def getPowerStationEventsWithCount(powerStationId: Long, offset: Int, limit: Int): Future[(Seq[PowerStationEvent], Int)] = {
    val composedAction = for {
      powerStations <- sql"SELECT amount, UNIX_TIMESTAMP(ts)*1000 FROM powerstationevents WHERE power_station_id = ${powerStationId} ORDER BY ts DESC LIMIT ${offset}, ${limit}"
        .as[(Double, Long)]
      totalCount <- sql"SELECT COUNT(*) FROM powerstationevents WHERE power_station_id = ${powerStationId}".as[Int].head
    } yield (powerStations, totalCount)
    db.run(composedAction.transactionally).map(t => (t._1.map(e => PowerStationEvent(e._1, e._2)), t._2))
  }

}
