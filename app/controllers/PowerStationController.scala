package controllers

import javax.inject.Inject

import daos.PowerStationDao
import exceptions.{PowerStationNotFoundException, TooLargeAmountException}
import models.{CreatePowerStation, PowerStation, PowerStationEvent}
import play.api.libs.json._
import play.api.mvc.Controller
import services.{PowerStationService, UserService}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class PowerStationController @Inject() (userService: UserService, powerStationDao: PowerStationDao, powerStationService: PowerStationService) extends Controller{

  def createPowerStation = AuthenticatedAction(userService).async(parse.json) { request =>
    request.body.validate[CreatePowerStation](CreatePowerStation.createPowerStationReads) match {
      case s: JsSuccess[CreatePowerStation] => {
        powerStationService.createPowerStation(s.value, request.user.id)
          .map(powerStation => Ok(Json.toJson(powerStation)(PowerStation.powerStationWrites)))
      }
      case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))
    }
  }
    def deletePowerStation(id: Long) = AuthenticatedAction(userService).async(parse.empty){ request =>
      powerStationDao.delete(id, request.user.id)
        .map(numberOfDeletes => {
          if(numberOfDeletes == 0) NotFound
          else NoContent
        })
    }

  def getPowerStations = AuthenticatedAction(userService).async(parse.empty){ request =>
    powerStationDao.getPowerStations(request.user.id)
      .map(powerStations => Ok(Json.toJson(powerStations)(Writes.seq(PowerStation.powerStationWrites))))
  }

  def loadPowerStation(id: Long) = AuthenticatedAction(userService).async(parse.json){ request =>
    request.body.validate[PowerStationEvent](PowerStationEvent.powerStationEventReads) match {
      case s: JsSuccess[PowerStationEvent] => {
        powerStationService.loadPowerStation(request.user.id, id, s.value).map {
          case Success(i: Int) => NoContent
          case Failure(e: PowerStationNotFoundException) => NotFound(Json.toJson(Map("error" -> s"power station not found : ${e.powerStationId}")))
          case Failure(_: TooLargeAmountException) => BadRequest(Json.toJson(Map("error" -> "amount too large")))
        }
      }
      case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))

    }
  }

  def consumePowerStation(id: Long) = AuthenticatedAction(userService).async(parse.json){ request =>
    request.body.validate[PowerStationEvent](PowerStationEvent.powerStationEventReads) match {
      case s: JsSuccess[PowerStationEvent] => {
        powerStationService.consumePowerStation(request.user.id, id, s.value).map {
          case Success(i: Int) => NoContent
          case Failure(e: PowerStationNotFoundException) => NotFound(Json.toJson(Map("error" -> s"power station not found : ${e.powerStationId}")))
          case Failure(_: TooLargeAmountException) => BadRequest(Json.toJson(Map("error" -> "amount too large")))
        }
      }
      case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))
    }
  }

  def getPowerStationEvents(id: Long) = AuthenticatedAction(userService).async(parse.empty) { request =>
    powerStationDao.getPowerStationEvents()
  }
}
