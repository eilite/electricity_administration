package controllers

import javax.inject.Inject

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import daos.UserDao
import models.UserSignup
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class UserController @Inject()(userService: UserService, userDao: UserDao) extends Controller {
  private val duplicateEntryMySQLCode: Int = 1062


  def signup = Action.async(parse.json) { request =>
    ActionUtils.parseJsonBody[UserSignup](request.body) { user =>
      userService.signupUser(user)
        .map {
          case Success(_) => NoContent
          case Failure(e: MySQLIntegrityConstraintViolationException) if(e.getErrorCode == duplicateEntryMySQLCode) =>
            BadRequest(Json.toJson(Map("error" -> "username already taken")))
        }
    }
  }

  def login = Action.async(parse.json) { request =>
    val unauthorized = Unauthorized(Json.toJson(Map("message" -> "wrong username or password")))
    ActionUtils.parseJsonBody[UserSignup](request.body) { user =>
      userDao.findByUsername(user.userName)
        .map {
          case Some(tuple) =>{
            val correctPwd = userService.validatePassword(user.password, tuple._2)
            if (correctPwd) Ok(Json.toJson(Map("token"-> userService.generateJwtToken(tuple._1))))
            else unauthorized
          }
          case None => unauthorized
        }
    }
  }
}
