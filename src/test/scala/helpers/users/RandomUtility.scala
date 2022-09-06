package helpers.users

import org.apache.commons.lang3.RandomStringUtils

import java.util.regex.Pattern

object RandomUtility {

  def randomString(length: Int = 8): String = {
    var randomString = ""
    do {
      randomString = RandomStringUtils.randomAlphanumeric(length)
    }
    while (!checkPassword(randomString))
    randomString
  }

  def checkPassword(s: String): Boolean = {
    /*Must be at least 8 characters, must contain at least 1 uppercase letter and 1 number*/
    val regex = "^(?=.*\\d)(?=.*[A-Z])(?=.*[A-Z]).{8,}$"
    val p = Pattern.compile(regex)
    p.matcher(s).matches()
  }
}
