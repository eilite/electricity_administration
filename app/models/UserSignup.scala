package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class UserSignup(userName: String, password: String)

object UserSignup{
  val userSignupReads: Reads[UserSignup]=(
      (JsPath \ "userName").read[String](Reads.minLength[String](3)) and
      (JsPath \ "password").read[String](Reads.minLength[String](3))
    )(UserSignup.apply _)
}

