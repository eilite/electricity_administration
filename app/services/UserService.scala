package services

import com.google.inject.ImplementedBy
import models.{User, UserSignup}

import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[DefaultUserService])
trait UserService {
  def decodeToken(token: String): Option[User]

  def generateJwtToken(user: User): String

  def signupUser(userSignup: UserSignup): Future[Try[Int]]

  def validatePassword(userPassword: String, hashedPwd: String): Boolean
}
