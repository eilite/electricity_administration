package controllers

import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.Future

object ParseAction{
  def parseJsonBody[BODY_TYPE](jsonBody: JsValue)(block: (BODY_TYPE => Future[Result]))(implicit rds: Reads[BODY_TYPE]): Future[Result] = {
    jsonBody.validate[BODY_TYPE] match {
      case s: JsSuccess[BODY_TYPE] => block(s.value)
      case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))
    }
  }
}