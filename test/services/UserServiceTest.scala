package services

import com.github.t3hnar.bcrypt._
import daos.{DefaultUserDao, UserDao}
import models.User
import org.mockito.Mockito.{mock, when}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.Configuration
import play.api.libs.json.Json

/**
  * Created by elie on 4/30/17.
  */
class UserServiceTest extends PlaySpec with BeforeAndAfter{

  private val userDao = mock(classOf[UserDao])
  private val config = mock(classOf[Configuration])
  private val underlyingConfig = mock(classOf[com.typesafe.config.Config])


  before({
    when(config.underlying).thenReturn(underlyingConfig)
    when(underlyingConfig.getString("jwtSecret")).thenReturn("secret")
    when(underlyingConfig.getString("bcryptsalt")).thenReturn("$2a$10$swCLHN1hBY1.N7.vr.uupe")
  })

  def fixture = new DefaultUserService(userDao, config)
  "decodeToken" must {
    "return none if token expired" in {
      val claim = JwtClaim(Json.stringify(Json.obj(("id", 1)))).expiresIn(-1)
      val token = JwtJson.encode(claim, "secret", JwtAlgorithm.HS256)
      assert(fixture.decodeToken(token).isEmpty)
    }
  }

  "return none if token invalid" in {
    val claim = JwtClaim(Json.stringify(Json.obj(("id",1)))).expiresIn(10)
    val token = JwtJson.encode(claim, "secret", JwtAlgorithm.HS256) + "whatever"
    assert(fixture.decodeToken(token).isEmpty)
  }

  "return user" in {
    val claim = JwtClaim(Json.stringify(Json.obj(("id", 1), ("name", "userName")))).expiresIn(10)
    val token = JwtJson.encode(claim, "secret", JwtAlgorithm.HS256)
    val userOption = fixture.decodeToken(token)
    assert(userOption.isDefined)
    assert(userOption.get.id == 1 && userOption.get.name == "userName")
  }

  "generate jwt token" must {
    "be different for two different users" in {
      assert(fixture.generateJwtToken(User(1, "user1")) != fixture.generateJwtToken(User(2, "user2")))
    }
    "be different for two different moments" in {
      val user = User(1, "user1")
      val token1 = fixture.generateJwtToken(user)
      Thread.sleep(1000)
      val token2 = fixture.generateJwtToken(user)
      assert(token1 != token2)
    }
  }

  "decode jwt token" must {
    "return true if token and password match" in {
      val password = "pwd"
      val hashed = password.bcrypt(underlyingConfig.getString("bcryptsalt"))
      assert(fixture.validatePassword(password, hashed))
    }

    "return false if token and password don't match" in {
      val password = "pwd"
      val hashed = "password".bcrypt(underlyingConfig.getString("bcryptsalt"))
      assert(!fixture.validatePassword(password, hashed))
    }
  }
}
