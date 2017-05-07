package controllers

import javax.inject.Inject

import daos.{PowerStationDao, PowerStationEventsDao}
import exceptions.{PowerStationNotFoundException, TooLargeAmountException}
import models.{PowerStationEvent, PowerStationEventsPage}
import play.api.libs.json.Json
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
    ActionUtils.parseJsonBody[PowerStationEvent](request.body)  { powerStationEvent =>
    powerStationDao.loadPowerStation(request.user.id, powerStationId, powerStationEvent.amount, powerStationEvent.timestamp).map {
        case Success(_) => NoContent
        case Failure(e: PowerStationNotFoundException) => NotFound(Json.toJson(Map("error" -> s"power station not found : ${e.powerStationId}")))
        case Failure(_: TooLargeAmountException) => BadRequest(Json.toJson(Map("error" -> "amount too large")))
      }
    }

  }

  def consumePowerStation(powerStationId: Long) = AuthenticatedAction(userService).async(parse.json) { request =>
    ActionUtils.parseJsonBody[PowerStationEvent](request.body) { powerStationEvent =>
      powerStationDao.consumeFromPowerStation(request.user.id, powerStationId, powerStationEvent.amount, powerStationEvent.timestamp).map {
        case Success(_) => NoContent
        case Failure(e: PowerStationNotFoundException) => NotFound(Json.toJson(Map("error" -> s"power station not found : ${e.powerStationId}")))
        case Failure(_: TooLargeAmountException) => BadRequest(Json.toJson(Map("error" -> "amount too large")))
      }

    }
  }

  def getPowerStationEvents(powerStationId: Long) = AuthenticatedAction(userService).async(parse.empty) { request =>
    val offset: Int = request.getQueryString("offset").map(_.toInt).getOrElse(0)
    val limit: Int = request.getQueryString("limit").map(_.toInt).getOrElse(10)
    powerStationDao.findPowerStation(request.user.id, powerStationId).flatMap {
      case Some(powerStation) => {
        powerStationEventsDao.getPowerStationEventsWithCount(powerStation.id, offset, limit)
          .map(t => Ok(Json.toJson(PowerStationEventsPage(t._1, offset, limit, t._2))))
      }
      case None => Future.successful(NotFound)
    }

  }
}