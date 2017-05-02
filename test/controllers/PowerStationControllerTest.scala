package controllers

import daos.PowerStationDao
import models.{CreatePowerStation, PowerStation, User}
import org.mockito.Mockito.{mock, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import services.{PowerStationService, UserService}

import scala.concurrent.Future

class PowerStationControllerTest extends PlaySpec with BeforeAndAfter with ScalaFutures{

  private val powerStationDao = mock(classOf[PowerStationDao])
  private val powerStationService = mock(classOf[PowerStationService])
  private val userService = mock(classOf[UserService])
  private val fixture = new PowerStationController(userService, powerStationDao, powerStationService )
  private val token = "token"
  private val userId = 2
  private val user = User(userId, "userName")

  before{
    when(userService.decodeToken(token)).thenReturn(Some(user))
  }

  "deletePowerStation" must {
    "return 401 if wrong token" in {
      val request: Request[Unit] = new FakeRequest[Unit]("DELETE", "/powerstations/1", FakeHeaders(Seq(("Authorization", "wrongtoken"))), Nil)
      when(userService.decodeToken("wrongtoken")).thenReturn(None)
      val result: Future[Result] = fixture.deletePowerStation(1).apply(request)
      assert(status(result) == 401)
    }
    "return 404 if powerStation does not exist" in {
      val request: Request[Unit] = new FakeRequest[Unit]("DELETE", "/powerstations/1", FakeHeaders(Seq(("Authorization", token))), Nil)

      when(powerStationDao.delete(1, userId)).thenReturn(Future.successful(0))
      val result: Future[Result] = fixture.deletePowerStation(1).apply(request)
      assert(status(result) == 404)
    }

    "return 200 for happy case" in {
      val request: Request[Unit] = new FakeRequest[Unit]("DELETE", "/powerstations/1", FakeHeaders(Seq(("Authorization", token))), Nil)
      when(powerStationDao.delete(1, userId)).thenReturn(Future.successful(1))
      val result: Future[Result] = fixture.deletePowerStation(1).apply(request)
      assert(status(result) == 204)
    }
  }

    "getPowerStations" must {

        "return 401 if wrong token" in {
          val request: Request[Unit] = new FakeRequest[Unit]("DELETE","/powerstations/1", FakeHeaders(Seq(("Authorization", "wrongtoken"))), Nil)
          when(userService.decodeToken("wrongtoken")).thenReturn(None)
          val result:Future[Result] = fixture.getPowerStations.apply(request)
          assert(status(result) == 401)
        }
      "return empty array if user does not have any powerstation" in {
        val request: Request[Unit] = new FakeRequest[Unit]("GET","/powerstations", FakeHeaders(Seq(("Authorization", token))), Nil)
        when(powerStationDao.getPowerStations(userId)).thenReturn(Future.successful(Seq()))

        val result:Future[Result] = fixture.getPowerStations.apply(request)
        assert(status(result)==200)
        assert(contentAsString(result).equals("[]"))
      }

      "return user's powerstations" in {
        val request: Request[Unit] = new FakeRequest[Unit]("GET","/powerstations", FakeHeaders(Seq(("Authorization", token))), Nil)
        when(powerStationDao.getPowerStations(userId)).thenReturn(Future.successful(Seq(PowerStation(13, "windturbine", 12234, 12))))

        val result:Future[Result] = fixture.getPowerStations.apply(request)
        assert(status(result)==200)
        val content = contentAsString(result)
        assert(content.contains("windturbine") && content.contains("12234") && content.contains("12234"))
      }
    }

    "createPowerStation" must {
      "return 401 if wrong token" in {
        val jsonBody =Json.obj(("powerStationType", "windmill"),("capacity", 1200))
        val request: Request[JsValue] = new FakeRequest[JsValue]("POST","/powerstations", FakeHeaders(Seq(("Authorization", "wrongtoken"))),jsonBody )
        when(userService.decodeToken("wrongtoken")).thenReturn(None)
        val result:Future[Result] = fixture.createPowerStation.apply(request)
        assert(status(result) == 401)
      }

      "return 400 if wrong json" in {
        val jsonBody =Json.obj(("wrongField", "windmill"),("capacity", 1200))
        val request: Request[JsValue] = new FakeRequest[JsValue]("POST","/powerstations", FakeHeaders(Seq(("Authorization", token))),jsonBody )
        val result:Future[Result] = fixture.createPowerStation.apply(request)
        assert(status(result) == 400)
      }
      "return 400 if validation fails" in {
        val jsonBody =Json.obj(("powerStationType", "windmill"),("capacity", -13))
        val request: Request[JsValue] = new FakeRequest[JsValue]("POST","/powerstations", FakeHeaders(Seq(("Authorization", token))),jsonBody )
        val result:Future[Result] = fixture.createPowerStation.apply(request)
        assert(status(result) == 400)
      }

      "return powerstation for happy case" in {
        val jsonBody =Json.obj(("powerStationType", "windmill"),("capacity", 12000))
        val request: Request[JsValue] = new FakeRequest[JsValue]("POST","/powerstations", FakeHeaders(Seq(("Authorization", token))), jsonBody)
        when(powerStationService.createPowerStation(CreatePowerStation("windmill", 12000), userId)).thenReturn(Future.successful(PowerStation(13,"windmill", 12000, 0)))
        val result:Future[Result] = fixture.createPowerStation.apply(request)
        assert(status(result) == 200)
        assert(contentAsString(result).contains("windmill") && contentAsString(result).contains("13") && contentAsString(result).contains("12000"))
      }

    }
}
