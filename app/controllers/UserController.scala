package controllers

import javax.inject.Inject

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import daos.UserDao
import models.UserSignup
import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import services.UserService

import scala.util.{Success, Failure}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UserController @Inject()(userService: UserService, userDao: UserDao) extends Controller {
  val duplicateEntryMySQLCode: Int = 1062
  val userSignupReads: Reads[UserSignup] =((
      (__ \ "userName").read[String](Reads.minLength[String](3)) and
      (__ \ "password").read[String](Reads.minLength[String](3))
    )(UserSignup))

  def signup = Action.async(parse.json) { request =>

    request.body.validate[UserSignup](userSignupReads)
      .map(userSignup => {
        userService.signupUser(userSignup)
          .map(_ => NoContent)
          .recover {
            case e: MySQLIntegrityConstraintViolationException if(e.getErrorCode == duplicateEntryMySQLCode)  =>{
             BadRequest(Json.toJson(Map("error" -> "username already taken")))
            }
          }
      })
      .recoverTotal(e => Future(BadRequest(JsError.toJson(e))))
  }

  def login = Action.async(parse.json) { request =>
    request.body.validate[UserSignup](userSignupReads)
      .map(userLogin => {
        userDao.findByUsername(userLogin.userName)
          .map {
            case Success(tuple) =>{
              val correctPwd = userService.validatePassword(userLogin.password, tuple._2)
              if (correctPwd) Ok(Json.toJson(Map("token"-> userService.generateJwtToken(tuple._1))))
              else Unauthorized
            }
            case Failure(e) => Unauthorized
          }
      }
      )
      .recoverTotal(e => Future(BadRequest(JsError.toJson(e))))
  }
}
