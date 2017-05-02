package controllers

import javax.inject.Inject

import daos.{PowerStationDao, PowerStationEventsDao}
import exceptions.{PowerStationNotFoundException, TooLargeAmountException}
import models.{PowerStationEvent, PowerStationWithEvents}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.Controller
import services.{PowerStationService, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class PowerStationEventsController @Inject() (userService: UserService,
                                              powerStationService: PowerStationService,
                                              powerStationDao: PowerStationDao,
                                              powerStationEventsDao: PowerStationEventsDao) extends Controller {

  def loadPowerStation(powerStationId: Long) = AuthenticatedAction(userService).async(parse.json) { request =>
    request.body.validate[PowerStationEvent](PowerStationEvent.powerStationEventReads) match {
      case s: JsSuccess[PowerStationEvent] => {
        powerStationService.loadPowerStation(request.user.id, powerStationId, s.value).map {
          case Success(_) => NoContent
          case Failure(e: PowerStationNotFoundException) => NotFound(Json.toJson(Map("error" -> s"power station not found : ${e.powerStationId}")))
          case Failure(_: TooLargeAmountException) => BadRequest(Json.toJson(Map("error" -> "amount too large")))
        }
      }
      case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))

    }
  }

  def consumePowerStation(powerStationId: Long) = AuthenticatedAction(userService).async(parse.json) { request =>
    request.body.validate[PowerStationEvent](PowerStationEvent.powerStationEventReads) match {
      case s: JsSuccess[PowerStationEvent] => {
        powerStationService.consumePowerStation(request.user.id, powerStationId, s.value).map {
          case Success(_) => NoContent
          case Failure(e: PowerStationNotFoundException) => NotFound(Json.toJson(Map("error" -> s"power station not found : ${e.powerStationId}")))
          case Failure(_: TooLargeAmountException) => BadRequest(Json.toJson(Map("error" -> "amount too large")))
        }
      }
      case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))
    }
  }

  def getPowerStationEvents(powerStationId: Long) = AuthenticatedAction(userService).async(parse.empty) { request =>
    powerStationDao.getPowerStation(request.user.id, powerStationId).flatMap {
      case Some(powerStation) => {
        powerStationEventsDao.getPowerStationEvents(powerStation.id)
          .map(events => Ok(Json.toJson(PowerStationWithEvents(powerStation, events))(PowerStationWithEvents.powerStationWithEventsWrite)))
      }
      case None => Future.successful(NotFound)
    }

  }
}
