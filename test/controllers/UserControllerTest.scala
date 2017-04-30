package controllers

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException
import daos.UserDao
import models.{User, UserSignup}
import org.mockito.Mockito.{mock, when}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import play.api.libs.json
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import services.UserService
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class UserControllerTest extends PlaySpec{

  val userService = mock(classOf[UserService])
  val userDao = mock(classOf[UserDao])
  val fixture = new UserController(userService, userDao)

  "signup endpoint" must {
    "return 400 on wrong json" in {
      val request = FakeRequest("POST", "/signup").withJsonBody(Json.obj(("userName", "name"), ("wrongField", "value")))

      fixture.signup.apply(request).map(response => assert(response.header.status == 400))
    }

    "return badrequest if username not unique" in {
      val request = FakeRequest("POST", "/signup").withJsonBody(Json.obj(("userName", "name"), ("password", "pwd")))

      when(userService.signupUser(UserSignup("name", "pwd")))
      .thenReturn(Future.successful(Failure(new MySQLIntegrityConstraintViolationException("duplicate","state",1062))))

      val result= fixture.signup.apply(request)
      result.map(response => {
        assert(response.header.status == 400)
        assert(response.body.dataStream.toString().contains("username already taken"))
      })
    }

    "return 204 for happy case" in {
      val request = FakeRequest("POST", "/signup").withJsonBody(Json.obj(("userName", "name"), ("password", "pwd")))

      when(userService.signupUser(UserSignup("name", "pwd"))).thenReturn(Future.successful(Success(1)))

      fixture.signup.apply(request).map(response => assert(response.header.status == 204))
    }
  }

  "login endpoint" must {
    "return badrequest on wrong json" in {
      val request = FakeRequest("POST", "/signup").withJsonBody(Json.obj(("userName", "name"), ("wrongField", "value")))

      fixture.login.apply(request).map(response => assert(response.header.status == 400))
    }

    "return 401 in case of non existing user" in {

      val request = FakeRequest("POST", "/signup").withJsonBody(Json.obj(("userName", "name"), ("password", "pwd")))

      when(userDao.findByUsername("userName")).thenReturn(Future.successful(None))

      fixture.login.apply(request).map(response => assert(response.header.status == 401))
    }

    "return 401 in case of wrong password" in {
      val request = FakeRequest("POST", "/signup").withJsonBody(Json.obj(("userName", "name"), ("password", "pwd")))

      when(userDao.findByUsername("userName")).thenReturn(Future.successful(Some((User(1, "userName"), "bddpwd"))))
      when(userService.validatePassword("pwd", "bddpwd")).thenReturn(false)

      fixture.login.apply(request).map(response => assert(response.header.status == 401))
    }

    "return 200 and token for happy case" in {
      val request = FakeRequest("POST", "/signup").withJsonBody(Json.obj(("userName", "userName"), ("password", "pwd")))

      when(userDao.findByUsername("userName")).thenReturn(Future.successful(Some((User(1, "userName"), "bddpwd"))))
      when(userService.validatePassword("pwd", "bddpwd")).thenReturn(true)
      when(userService.generateJwtToken(User(1, "userName"))).thenReturn("jwttoken")

      fixture.login.apply(request).map(response => {
        assert(response.header.status == 200)
        assert(response.body.toString.contains("jwttoken"))
      })
    }


  }
}
