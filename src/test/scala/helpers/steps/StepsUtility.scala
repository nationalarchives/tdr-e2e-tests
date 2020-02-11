package helpers.steps

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

  def userLogin(webDriver: WebDriver, userName: String, password: String) = {
    enterUserCredentials(webDriver, userName, password)
    val clickableElement = webDriver.findElement(By.cssSelector("[name='login']"))
    clickableElement.click()
  }

  def enterUserCredentials(webDriver: WebDriver, userName: String, password: String) = {
    val userNameElement = webDriver.findElement(By.cssSelector("[name='username']"))
    val passwordElement = webDriver.findElement(By.cssSelector("[name='password']"))

    userNameElement.sendKeys(userName)
    passwordElement.sendKeys(password)
  }
}
