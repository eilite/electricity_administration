package daos

import javax.inject.{Inject, Singleton}

import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}


@Singleton
class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  def findByUsername(userName: String): Future[Option[(User, String)]]= {
    db.run(sql"select id, username, pwd from users where username = ${userName}".as[(Int, String, String)])
      .map {
        case results if(results.size == 1)=> Some((User(results.head._1, results.head._2), results.head._3))
        case _ => None
      }

  }

  def insert(userName: String, password: String): Future[Try[Int]] =
    db.run(sqlu"insert into users(username, pwd) values (${userName}, ${password})".asTry)
}
