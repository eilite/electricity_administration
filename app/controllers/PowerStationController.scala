package controllers

import javax.inject.Inject

import daos.PowerStationDao
import models.{CreatePowerStation, PowerStation}
import play.api.libs.json._
import play.api.mvc.Controller
import services.UserService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class PowerStationController @Inject() (userService: UserService, powerStationDao: PowerStationDao) extends Controller{
  def createPowerStation = AuthenticatedAction(userService).async(parse.json) { request =>
    request.body.validate[CreatePowerStation](CreatePowerStation.createPowerStationReads) match {
      case s: JsSuccess[CreatePowerStation] => {
        powerStationDao.insert(request.user.id, s.value.powerStationType, s.value.capacity)
          .map(powerStationId =>PowerStation(powerStationId, s.value.powerStationType, s.value.capacity))
          .map(powerStation => Ok(Json.toJson(powerStation)(PowerStation.powerStationWrites)))
      }
      case e: JsError => Future(BadRequest(JsError.toJson(e)))
    }
  }
    def deletePowerStation(id: Long) = AuthenticatedAction(userService).async(parse.empty){request =>
      powerStationDao.delete(id, request.user.id).map(numberOfDeletes => {
        if(numberOfDeletes == 0) NotFound
        else Ok
      })
    }

  def getPowerStations = AuthenticatedAction(userService).async(parse.empty){request =>
    Future.successful(Ok)
  }

}
