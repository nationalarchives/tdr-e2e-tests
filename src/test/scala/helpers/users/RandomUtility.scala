package helpers.users

import org.apache.commons.lang3.RandomStringUtils

import scala.util.Random

object RandomUtility {

  def randomString(length: Int = 8): String = {
//    Random.alphanumeric.dropWhile(_.isDigit).take(length).mkString
    RandomStringUtils.randomAlphanumeric(length)
  }
}
