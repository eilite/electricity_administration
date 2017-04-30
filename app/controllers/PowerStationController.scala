package controllers

import javax.inject.Inject

import play.api.libs.json._
import play.api.mvc.Controller
import services.UserService

import scala.concurrent.Future


class PowerStationController @Inject() (userService: UserService) extends Controller{

  def createPowerStation = new AuthenticatedAction(userService).async { request =>
    Future.successful(Ok(Json.toJson(Map("userId" -> request.user.id))))
  }
}
