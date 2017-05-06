package models

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Reads, Writes, __}
import play.api.libs.functional.syntax._


case class PowerStationEvent(amount: Double, timestamp: Long)

object PowerStationEvent{
  implicit val powerStationEventReads: Reads[PowerStationEvent] =(
    (__ \ "amount").read[Double](Reads.min[Double](0)) and
      (__ \ "timestamp").read[Long](Reads.min[Long](0))
    )(PowerStationEvent.apply _)

  implicit val powerStationEventWrites: Writes[PowerStationEvent] =(
    (JsPath \ "amount").write[Double] and
      (JsPath \ "timestamp").write[Long]
    )(unlift(PowerStationEvent.unapply))
}

case class PowerStationWithEvents(id: Long,
                                  powerStationType: String,
                                  capacity: Double,
                                  stock: Double,
                                  events: Seq[PowerStationEvent])

object PowerStationWithEvents{

  def apply(powerStation: PowerStation,
            events: Seq[PowerStationEvent]): PowerStationWithEvents =
    new PowerStationWithEvents(powerStation.id, powerStation.powerStationType, powerStation.capacity, powerStation.stock, events)

  implicit val powerStationWithEventsWrite: Writes[PowerStationWithEvents] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "powerStationType").write[String] and
      (JsPath \ "capacity").write[Double] and
      (JsPath \ "stock").write[Double] and
      (JsPath \ "events").write(Writes.seq[PowerStationEvent])
    )(unlift(PowerStationWithEvents.unapply))

}

case class PowerStationEventsPage(events: Seq[PowerStationEvent], limit: Int, offset: Int, count: Int)

object PowerStationEventsPage{

  implicit val powerStationEventsPagedWrite: Writes[PowerStationEventsPage] = (
    (JsPath \ "events").write(Writes.seq[PowerStationEvent]) and
      (JsPath \ "offset").write[Int] and
      (JsPath \ "limit").write[Int] and
      (JsPath \ "count").write[Int]
    )(unlift(PowerStationEventsPage.unapply))
}
