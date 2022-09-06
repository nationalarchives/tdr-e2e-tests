package helpers.users

import org.apache.commons.lang3.RandomStringUtils

import java.util.regex.Pattern
import scala.util.Random

object RandomUtility {

  def randomString(length: Int = 8): String = {
//    Random.alphanumeric.dropWhile(_.isDigit).take(length).mkString
    var randomString = ""
    do {
      randomString = RandomStringUtils.randomAlphanumeric(length)
    }
    while (!checkPassword(randomString))
    randomString
  }

  def checkPassword(s: String): Boolean = {
    /*Check password contains:
    - at least 8 characters
    - must contain at least 1 uppercase letter, 1 lowercase letter, and 1 number
    - Can contain special characters*/
    val regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$"
    val p = Pattern.compile(regex)
    p.matcher(s).matches()
  }
}
