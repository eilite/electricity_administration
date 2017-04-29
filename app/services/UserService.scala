package services

import java.sql.Timestamp
import java.time.Instant
import javax.inject.Inject

import daos.UserDao
import models.{User, UserSignup}
import com.github.t3hnar.bcrypt._
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by elie on 4/29/17.
  */
class UserService @Inject()(userDao: UserDao, configuration: Configuration) {
  /**
    * Generate jwt based on user's infos
    * @param user
    * @return
    */
  def generateJwtToken(user: User): String = {
    val claim = Json.obj(("id", user.id), ("name", user.name), ("issuedAt", Timestamp.from(Instant.now).getTime))
    JwtJson.encode(claim, configuration.underlying.getString("jwtSecret"), JwtAlgorithm.HS256)
  }


  def signupUser(userSignup: UserSignup): Future[Unit] ={
    userDao.insert(userSignup.userName, userSignup.password.bcrypt)
      .map(_ => Unit)
  }

  def validatePassword(userPassword: String, hashedPwd: String): Boolean ={
    userPassword.isBcrypted(hashedPwd)
  }
}
