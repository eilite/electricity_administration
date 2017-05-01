package controllers

import javax.inject.Inject

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import daos.UserDao
import models.UserSignup
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class UserController @Inject()(userService: UserService, userDao: UserDao) extends Controller {
  private val duplicateEntryMySQLCode: Int = 1062


  def signup = Action.async(parse.json) { request =>
    request.body.validate[UserSignup](UserSignup.userSignupReads) match {
      case s: JsSuccess[UserSignup]=> {
        userService.signupUser(s.value)
          .map {
            case Success(_) => NoContent
            case Failure(e: MySQLIntegrityConstraintViolationException) if(e.getErrorCode == duplicateEntryMySQLCode) =>
              BadRequest(Json.toJson(Map("error" -> "username already taken")))
          }
      }
      case e: JsError => Future(BadRequest(JsError.toJson(e)))
    }
  }

  def login = Action.async(parse.json) { request =>
    val unauthorized = Unauthorized(Json.toJson(Map("message" -> "wrong username or password")))
    request.body.validate[UserSignup](UserSignup.userSignupReads) match {
      case s: JsSuccess[UserSignup] => {
        userDao.findByUsername(s.value.userName)
          .map {
            case Some(tuple) =>{
              val correctPwd = userService.validatePassword(s.value.password, tuple._2)
              if (correctPwd) Ok(Json.toJson(Map("token"-> userService.generateJwtToken(tuple._1))))
              else unauthorized
            }
            case None => unauthorized
          }
      }
      case e:JsError => Future(BadRequest(JsError.toJson(e)))
    }
  }
}
