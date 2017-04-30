package services

import javax.inject.Inject

import com.github.t3hnar.bcrypt._
import daos.UserDao
import models.{User, UserSignup}
import pdi.jwt._
import pdi.jwt.JwtJson._
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class UserService @Inject()(userDao: UserDao, configuration: Configuration) {
  private val jwtSecret = configuration.underlying.getString("jwtSecret")
  private val bcryptsalt = configuration.underlying.getString("bcryptsalt")
  private val jwtAlgorithm = JwtAlgorithm.HS256
  private val jwtExpiresIn = 24 * 60 * 60

  /**
    * Decode jwt token and extract user
    * @param token
    * @return
    */
  def decodeToken(token: String): Option[User] = {
    JwtJson.decode(token, jwtSecret, Seq(jwtAlgorithm))
      .filter(_.isValid)
      .map(Json.toJson(_))
      .map(jsonClaim => User((jsonClaim \ "id").as[Int], (jsonClaim \ "name").as[String]))
      .toOption
  }

  /**
    * Generate jwt based on user's infos
    *
    * @param user
    * @return
    */
  def generateJwtToken(user: User): String = {
    val jsonUser = Json.obj(("id", user.id), ("name", user.name))
    val claim = JwtClaim(Json.stringify(jsonUser)).issuedNow.expiresIn(jwtExpiresIn)
    JwtJson.encode(claim, jwtSecret, jwtAlgorithm)
  }

  def signupUser(userSignup: UserSignup): Future[Try[Int]] = {
    userDao.insert(userSignup.userName, userSignup.password.bcrypt(bcryptsalt))
  }

  def validatePassword(userPassword: String, hashedPwd: String): Boolean = {
    userPassword.isBcrypted(hashedPwd)
  }
}
