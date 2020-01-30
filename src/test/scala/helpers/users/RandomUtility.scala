package helpers.users

import scala.util.Random

object RandomUtility {

  def randomString(length: Int = 8): String = {
    Random.alphanumeric.dropWhile(_.isDigit).take(length).mkString
  }
}
