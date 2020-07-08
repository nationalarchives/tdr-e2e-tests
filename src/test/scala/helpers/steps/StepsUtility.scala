package helpers.steps

import helpers.users.UserCredentials
import org.openqa.selenium.{By, WebDriver}
import org.openqa.selenium.chrome.ChromeOptions

object StepsUtility {

  val getChromeOptions: ChromeOptions = {
    val chromeOptions: ChromeOptions = new ChromeOptions
    val chromeDriverLocation = System.getenv("CHROME_DRIVER")

    chromeOptions.setHeadless(true)
    chromeOptions.addArguments("--no-sandbox")
    chromeOptions.addArguments("--disable-dev-shm-usage")
    chromeOptions.addArguments("--verbose")
    System.setProperty("webdriver.chrome.driver", chromeDriverLocation)

    chromeOptions
  }

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

  def elementHasClassHide(targetIdName: String, webDriver: WebDriver): Boolean = {
    val id = targetIdName.replaceAll(" ", "-")
    val element = webDriver.findElement(By.cssSelector(s"#$id"))
    element.getAttribute("class").contains("hide")
  }
}
