import javax.inject.Singleton

import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results._
import play.api.Logger

import scala.concurrent.Future

@Singleton
class ErrorHandler extends HttpErrorHandler{
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(Status(statusCode)(Json.toJson(Map("error" -> message))))
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Logger.error("error occured", exception)
    Future.successful(InternalServerError(Json.toJson(Map("error" -> "internal server error occured"))))
  }
}
