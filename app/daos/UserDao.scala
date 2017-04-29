package daos

import javax.inject.{Inject, Singleton}

import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.Try

@Singleton
class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  def findByUsername(userName: String): Future[Try[(User, String)]]= {
    db.run(sql"select id, username, pwd from Users where username = ${userName}".as[(Int, String, String)])
      .map(results => Try((User(results.head._1, results.head._2), results.head._3)))

  }


  def insert(userName: String, password: String): Future[Int] =
    db.run(sqlu"insert into Users(username, pwd) values (${userName}, ${password})")

}
