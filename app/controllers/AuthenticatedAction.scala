package controllers


import models.User
import play.api.mvc._
import services.UserService

import scala.concurrent.Future

class AuthenticatedAction(userService: UserService)  extends ActionBuilder[AuthenticatedRequest]{

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    request.headers
      .get("Authorization")
      .flatMap(token => {
        userService.decodeToken(token)
          .map(user => block(new AuthenticatedRequest(user, request)))
      })
      .getOrElse(Future.successful(Results.Unauthorized))
  }
}

class AuthenticatedRequest[A](val user: User, val request: Request[A]) extends WrappedRequest[A](request)