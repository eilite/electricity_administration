package util

import com.github.t3hnar.bcrypt._
/**
  * Created by elie on 4/29/17.
  */
class Utils {

  def encrypt(pwd: String): String = pwd.bcrypt

}
