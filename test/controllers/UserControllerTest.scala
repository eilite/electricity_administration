package controllers

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import daos.UserDao
import models.{User, UserSignup}
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import services.UserService

import scala.concurrent.Future
import scala.util.{Failure, Success}

class UserControllerTest extends PlaySpec{

  private val userService = mock(classOf[UserService])
  private val userDao = mock(classOf[UserDao])
  private val fixture = new UserController(userService, userDao)


  "signup endpoint" must {
    "return 400 if wrong json" in {
      val jsonBody =Json.obj(("userName", "name"),("wrongField", "value"))
      val request = new FakeRequest[JsValue]("POST","/signup",FakeHeaders(Seq()), jsonBody)
      val result:Future[Result] = fixture.signup.apply(request)
      assert(status(result) == 400)
    }

    "return badrequest if username not unique" in {
      when(userService.signupUser(UserSignup("name", "pwd")))
      .thenReturn(Future.successful(Failure(new MySQLIntegrityConstraintViolationException("duplicate","state",1062))))
      val jsonBody =Json.obj(("userName", "name"), ("password", "pwd"))
      val request = new FakeRequest[JsValue]("POST","/signup",FakeHeaders(Seq()), jsonBody)

      val result:Future[Result] = fixture.signup.apply(request)
      assert(contentAsString(result).contains("username already taken"))
      assert(status(result) == 400)
    }

    "return 204 for happy case" in {
      val jsonBody =Json.obj(("userName", "name"), ("password", "pwd"))
      val request = new FakeRequest[JsValue]("POST","/signup",FakeHeaders(Seq()), jsonBody)
      when(userService.signupUser(UserSignup("name", "pwd"))).thenReturn(Future.successful(Success(1)))
      val result:Future[Result] = fixture.signup.apply(request)
      assert(status(result)==204)
    }
  }

  "login endpoint" must {
    "return badrequest on wrong json" in {
      val jsonBody =Json.obj(("userName", "name"),("wrongField", "value"))
      val request = new FakeRequest[JsValue]("POST","/login",FakeHeaders(Seq()), jsonBody)
      val result:Future[Result] = fixture.login.apply(request)
      assert(status(result) == 400)
    }

    "return 401 in case of non existing user" in {
      val jsonBody =Json.obj(("userName", "name"),("password", "pwd"))
      val request = new FakeRequest[JsValue]("POST","/signup",FakeHeaders(Seq()), jsonBody)

      when(userDao.findByUsername("name")).thenReturn(Future.successful(None))
      val result:Future[Result] = fixture.login.apply(request)
      assert(status(result)==401)
    }

    "return 401 in case of wrong password" in {
      val jsonBody =Json.obj(("userName", "userName"),("password", "pwd"))
      val request = new FakeRequest[JsValue]("POST","/signup",FakeHeaders(Seq()), jsonBody)

      when(userDao.findByUsername("userName")).thenReturn(Future.successful(Some((User(1, "userName"), "bddpwd"))))
      when(userService.validatePassword("pwd", "bddpwd")).thenReturn(false)
      val result:Future[Result] = fixture.login.apply(request)
      assert(status(result)==401)
    }

    "return 200 and token for happy case" in {
      val jsonBody =Json.obj(("userName", "userName"),("password", "pwd"))
      val request = new FakeRequest[JsValue]("POST","/signup",FakeHeaders(Seq()), jsonBody)

      when(userDao.findByUsername("userName")).thenReturn(Future.successful(Some((User(1, "userName"), "bddpwd"))))
      when(userService.validatePassword("pwd", "bddpwd")).thenReturn(true)
      when(userService.generateJwtToken(User(1, "userName"))).thenReturn("jwttoken")
      val result:Future[Result] = fixture.login.apply(request)
      assert(status(result)==200)
      assert(contentAsString(result).contains("jwttoken"))
    }


  }
}
