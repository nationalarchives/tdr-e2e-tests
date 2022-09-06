package helpers.users

import org.apache.commons.lang3.RandomStringUtils

import java.util.regex.Pattern
import scala.annotation.tailrec

object RandomUtility {

  @tailrec
  def randomString(length: Int = 8): String = {
    val stringToCheck = RandomStringUtils.randomAlphanumeric(length)
    if(checkPassword(stringToCheck)) {
      stringToCheck
    } else {
      randomString(length)
    }
  }

  def checkPassword(s: String): Boolean = {
    /*Must be at least 8 characters, must contain at least 1 uppercase letter and 1 number*/
    val regex = "^(?=.*\\d)(?=.*[A-Z])(?=.*[A-Z]).{8,}$"
    val p = Pattern.compile(regex)
    p.matcher(s).matches()
  }
}
