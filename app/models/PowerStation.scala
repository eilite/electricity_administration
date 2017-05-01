package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._



case class PowerStation(id: Long, powerStationType: String, capacity: Double)

object PowerStation{
  val powerStationWrites: Writes[PowerStation]=(
    (JsPath \ "id").write[Long] and
    (JsPath \ "powerStationType").write[String] and
    (JsPath \ "capacity").write[Double]
  )(unlift(PowerStation.unapply))
}
case class CreatePowerStation(powerStationType: String, capacity: Double)

object CreatePowerStation{
  val createPowerStationReads: Reads[CreatePowerStation] =(
    (__ \ "powerStationType").read[String](Reads.minLength[String](3)) and
      (__ \ "capacity").read[Double](Reads.min[Double](0))
    )(CreatePowerStation.apply _)
}
