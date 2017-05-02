package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._



case class PowerStation(id: Long, powerStationType: String, capacity: Double, stock: Double)

object PowerStation{
  val powerStationWrites: Writes[PowerStation]=(
    (JsPath \ "id").write[Long] and
    (JsPath \ "powerStationType").write[String] and
    (JsPath \ "capacity").write[Double] and
    (JsPath \ "stock").write[Double]
  )(unlift(PowerStation.unapply))
}
case class CreatePowerStation(powerStationType: String, capacity: Double)

object CreatePowerStation{
  val createPowerStationReads: Reads[CreatePowerStation] =(
      (__ \ "powerStationType").read[String](Reads.minLength[String](3)) and
      (__ \ "capacity").read[Double](Reads.min[Double](0))
    )(CreatePowerStation.apply _)
}

case class PowerStationEvent(amount: Double, timestamp: Long)

object PowerStationEvent{
  val powerStationEventReads: Reads[PowerStationEvent] =(
      (__ \ "amount").read[Double](Reads.min[Double](0)) and
      (__ \ "timestamp").read[Long](Reads.min[Long](0))
    )(PowerStationEvent.apply _)

  val powerStationEventWrites: Writes[PowerStationEvent] =(
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

  val powerStationWithEventsWrite: Writes[PowerStationWithEvents] = (
    (JsPath \ "id").write[Long] and
    (JsPath \ "powerStationType").write[String] and
    (JsPath \ "capacity").write[Double] and
    (JsPath \ "stock").write[Double] and
    (JsPath \ "events").write(Writes.seq[PowerStationEvent](PowerStationEvent.powerStationEventWrites))
    )(unlift(PowerStationWithEvents.unapply))

}
