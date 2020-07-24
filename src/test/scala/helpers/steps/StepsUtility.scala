package helpers.steps

import helpers.users.UserCredentials
import org.openqa.selenium.{By, WebDriver}
import org.openqa.selenium.chrome.ChromeOptions

object StepsUtility {

  def userLogin(webDriver: WebDriver, userCredentials: UserCredentials) = {
    enterUserCredentials(webDriver, userCredentials)
    val clickableElement = webDriver.findElement(By.cssSelector("[name='login']"))
    clickableElement.click()
  }

  def enterUserCredentials(webDriver: WebDriver, userCredentials: UserCredentials) = {
    val userNameElement = webDriver.findElement(By.cssSelector("[name='username']"))
    val passwordElement = webDriver.findElement(By.cssSelector("[name='password']"))

    userNameElement.sendKeys(userCredentials.userName)
    passwordElement.sendKeys(userCredentials.password)
  }

  def elementHasClassHide(id: String, webDriver: WebDriver): Boolean = {
    println("Element Has Class Hide URL: " + webDriver.getCurrentUrl)
    val element = webDriver.findElement(By.cssSelector(s"#$id"))
    element.getAttribute("class").contains("hide")
  }
}
