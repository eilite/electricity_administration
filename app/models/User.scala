package models

import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

case class User(id: Long, name: String)

case class UserSignup(userName: String, password: String)

object UserSignup{
  val userSignupReads: Reads[UserSignup]=(
    (JsPath \ "userName").read[String](Reads.minLength[String](3)) and
      (JsPath \ "password").read[String](Reads.minLength[String](3))
    )(UserSignup.apply _)
}

