package daos

import javax.inject.{Inject, Singleton}

import models.{PowerStation, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}


@Singleton
class UserDao @Inject()(val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  def findByUsername(userName: String): Future[Option[(User, String)]]= {
    db.run(sql"SELECT id, username, pwd FROM users WHERE username = ${userName}".as[(Long, String, String)])
      .map {
        case results if(results.size == 1)=> Some((User(results.head._1, results.head._2), results.head._3))
        case _ => None
      }

  }

  def insert(userName: String, password: String): Future[Try[Int]] =
    db.run(sqlu"INSERT INTO users(username, pwd) VALUES (${userName}, ${password})".asTry)

}
