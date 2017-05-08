package daos

import com.google.inject.ImplementedBy
import models.User

import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[DefaultUserDao])
trait UserDao {
  def findByUsername(userName: String): Future[Option[(User, String)]]

  def insert(userName: String, password: String): Future[Try[Int]]
}
