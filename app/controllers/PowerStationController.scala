
package controllers

import javax.inject.Inject

import daos.PowerStationDao
import exceptions.PowerStationNotFoundException
import models.{CreatePowerStation, PowerStation, PowerStationWithEvents}
import play.api.libs.json._
import play.api.mvc.Controller
import services.{PowerStationService, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class PowerStationController @Inject() (userService: UserService,
                                        powerStationDao: PowerStationDao,
                                        powerStationService: PowerStationService) extends Controller{

  def createPowerStation = AuthenticatedAction(userService).async(parse.json) { request =>
    ActionUtils.parseJsonBody[CreatePowerStation](request.body) { powerStation =>
      powerStationService.createPowerStation(powerStation, request.user.id)
        .map(powerStation => Ok(Json.toJson(powerStation)))
    }
  }
  
  def deletePowerStation(powerStationId: Long) = AuthenticatedAction(userService).async(parse.empty){ request =>
    powerStationDao.delete(powerStationId, request.user.id)
      .map(numberOfDeletes => {
        if(numberOfDeletes == 0) NotFound
        else NoContent
      })
  }

  def updatePowerStation(powerStationId: Long) = AuthenticatedAction(userService).async(parse.json){ request =>
   ActionUtils.parseJsonBody[CreatePowerStation](request.body) { powerStation =>
     powerStationDao.update(request.user.id, powerStationId, powerStation.powerStationType, powerStation.capacity)
      .map{
        case Success(powerStation: PowerStation) => Ok(Json.toJson(powerStation))
        case Failure(e: PowerStationNotFoundException) => NotFound(Json.toJson(Map("error" -> s"power station not found : ${e.powerStationId}")))
      }
   }
  }

  def getPowerStation(id: Long) = AuthenticatedAction(userService).async(parse.empty){request =>
    powerStationService.getPowerStationWithFirstEvents(request.user.id, id).map {
      case Some(powerStationWithEvents: PowerStationWithEvents) => Ok(Json.toJson(powerStationWithEvents))
      case None => NotFound
    }
  }

  def getPowerStations = AuthenticatedAction(userService).async(parse.empty){ request =>
    powerStationDao.getPowerStations(request.user.id)
      .map(powerStations => Ok(Json.toJson(powerStations)))
  }

}
