package controllers

import daos.{PowerStationDao, PowerStationEventsDao}
import exceptions.{PowerStationNotFoundException, TooLargeAmountException}
import models.{PowerStation, PowerStationEvent, User}
import org.mockito.Mockito.{mock, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Request, Result}
import play.api.test.Helpers.{status, _}
import play.api.test.{FakeHeaders, FakeRequest}
import services.{PowerStationService, UserService}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PowerStationEventsControllerTest extends PlaySpec with ScalaFutures with BeforeAndAfter{
  private val userService: UserService = mock(classOf[UserService])
  private val powerStationService=  mock(classOf[PowerStationService])
  private val powerStationDao = mock(classOf[PowerStationDao])
  private val powerStationEventsDao = mock(classOf[PowerStationEventsDao])
  private val token = "token"
  private val userId = 2
  private val user = User(userId, "userName")
  private val fixture = new PowerStationEventsController(userService, powerStationService, powerStationDao, powerStationEventsDao)

  before {
    when(userService.decodeToken(token)).thenReturn(Some(user))
  }

  "loadPowerStation" must {
    "return 401 if wrong token" in {
      val jsonBody = Json.obj(("amount", 200),("timestamp", System.currentTimeMillis))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", "wrongtoken"))), jsonBody)
      when(userService.decodeToken("wrongtoken")).thenReturn(None)
      val result: Future[Result] = fixture.loadPowerStation(1).apply(request)
      assert(status(result) == 401)
    }

    "return 400 if wrong json" in {
      val jsonBody = Json.obj(("amount", 200),("wronfField", System.currentTimeMillis))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", token))), jsonBody)

      val result: Future[Result] = fixture.loadPowerStation(1).apply(request)
      assert(status(result) == 400)
    }

    "return 404 if powerstation not found" in {
      val time = System.currentTimeMillis
      val jsonBody = Json.obj(("amount", 200),("timestamp", time))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", token))), jsonBody)

      when(powerStationService.loadPowerStation(userId, 1, PowerStationEvent(200, time))).thenReturn(Future.successful(Failure(new PowerStationNotFoundException(1))))

      val result: Future[Result] = fixture.loadPowerStation(1).apply(request)
      assert(status(result) == 404)
    }

    "return 400 if amount too large not found" in {
      val time = System.currentTimeMillis
      val jsonBody = Json.obj(("amount", 200),("timestamp", time))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", token))), jsonBody)

      when(powerStationService.loadPowerStation(userId, 1, PowerStationEvent(200, time))).thenReturn(Future.successful(Failure(new TooLargeAmountException)))

      val result: Future[Result] = fixture.loadPowerStation(1).apply(request)
      assert(status(result) == 400)
    }

    "return 204 for happy case" in {
      val time = System.currentTimeMillis
      val jsonBody = Json.obj(("amount", 200),("timestamp", time))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", token))), jsonBody)

      when(powerStationService.loadPowerStation(userId, 1, PowerStationEvent(200, time))).thenReturn(Future.successful(Success(1)))

      val result: Future[Result] = fixture.loadPowerStation(1).apply(request)
      assert(status(result) == 204)
    }
  }

  "consumePowerStation" must {
    "return 401 if wrong token" in {
      val jsonBody = Json.obj(("amount", 200),("timestamp", System.currentTimeMillis))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", "wrongtoken"))), jsonBody)
      when(userService.decodeToken("wrongtoken")).thenReturn(None)
      val result: Future[Result] = fixture.consumePowerStation(1).apply(request)
      assert(status(result) == 401)
    }

    "return 400 if wrong json" in {
      val jsonBody = Json.obj(("amount", 200),("wronfField", System.currentTimeMillis))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", token))), jsonBody)

      val result: Future[Result] = fixture.consumePowerStation(1).apply(request)
      assert(status(result) == 400)
    }

    "return 404 if powerstation not found" in {
      val time = System.currentTimeMillis
      val jsonBody = Json.obj(("amount", 200),("timestamp", time))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", token))), jsonBody)

      when(powerStationService.consumePowerStation(userId, 1, PowerStationEvent(200, time))).thenReturn(Future.successful(Failure(new PowerStationNotFoundException(1))))

      val result: Future[Result] = fixture.consumePowerStation(1).apply(request)
      assert(status(result) == 404)
    }

    "return 400 if amount too large not found" in {
      val time = System.currentTimeMillis
      val jsonBody = Json.obj(("amount", 200),("timestamp", time))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", token))), jsonBody)

      when(powerStationService.consumePowerStation(userId, 1, PowerStationEvent(200, time))).thenReturn(Future.successful(Failure(new TooLargeAmountException)))

      val result: Future[Result] = fixture.consumePowerStation(1).apply(request)
      assert(status(result) == 400)
    }

    "return 204 for happy case" in {
      val time = System.currentTimeMillis
      val jsonBody = Json.obj(("amount", 200),("timestamp", time))
      val request: Request[JsValue] = new FakeRequest[JsValue]("POST", "/powerstations/1/load", FakeHeaders(Seq(("Authorization", token))), jsonBody)

      when(powerStationService.consumePowerStation(userId, 1, PowerStationEvent(200, time))).thenReturn(Future.successful(Success(1)))

      val result: Future[Result] = fixture.consumePowerStation(1).apply(request)
      assert(status(result) == 204)
    }
  }

  "getPowerStationEvents" must {
    "return 404 if powerstation not found " in {

      val request: Request[Unit] = new FakeRequest[Unit]("POST", "/powerstations/1", FakeHeaders(Seq(("Authorization", token))), Nil)
      when(powerStationDao.getPowerStation(userId, 1)).thenReturn(Future.successful(None))

      val result: Future[Result] = fixture.getPowerStationEvents(1).apply(request)
      assert(status(result) == 404)
    }

    "return powerstations" in {
      val request: Request[Unit] = new FakeRequest[Unit]("POST", "/powerstations/1", FakeHeaders(Seq(("Authorization", token))), Nil)
      when(powerStationDao.getPowerStation(userId, 1)).thenReturn(Future.successful(Some(PowerStation(1, "powerstationtype", 13000, 1234))))
      when(powerStationEventsDao.getPowerStationEvents(1)).thenReturn(Future.successful(Seq(PowerStationEvent(1234, System.currentTimeMillis()))))

      val result: Future[Result] = fixture.getPowerStationEvents(1).apply(request)
      assert(status(result) == 200)
      assert(contentAsString(result).contains("1234") && contentAsString(result).contains("powerstationtype"))
    }
  }

}
