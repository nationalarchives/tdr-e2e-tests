package steps

import com.typesafe.config.ConfigFactory
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.steps.StepsUtility
import helpers.users.{KeycloakClient, RandomUtility}
import org.junit.Assert
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.Matchers

class LoginSteps extends ScalaDsl with EN with Matchers {
  var webDriver: WebDriver = _
  var userId: String = ""

  val configuration = ConfigFactory.load()
  val baseUrl: String = configuration.getString("tdr.base.url")
  val authUrl: String = configuration.getString("tdr.auth.url")
  val userName: String = RandomUtility.randomString()
  val password: String = RandomUtility.randomString(10)
  val otherPageUrl: String = configuration.getString("redirect.base.url")

  Before() { scenario =>
    webDriver = new ChromeDriver(StepsUtility.getChromeOptions)
  }

  After() { scenario =>
    webDriver.quit()
    KeycloakClient.deleteUser(userId)
  }

  Given("^A logged out user") {
    userId = KeycloakClient.createUser(userName, password)
  }

  When("^the logged out user visits url") {
    webDriver.get(s"$baseUrl")
  }

  And("^the logged out user clicks the (.*) element$") {
    selector: String =>
      val clickableElement = webDriver.findElement(By.cssSelector(selector))
      clickableElement.click()
  }

  Then("^the logged out user should be at the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl

      Assert.assertTrue(currentUrl.startsWith(s"$authUrl/$page"))
  }

  Then("^the logged out user enters valid credentials") {

    val userNameElement = webDriver.findElement(By.cssSelector("[name='username']"))
    val passwordElement = webDriver.findElement(By.cssSelector("[name='password']"))

    userNameElement.sendKeys(userName)
    passwordElement.sendKeys(password)
  }

  Then("^the logged in user should be at the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl

      Assert.assertTrue(currentUrl.startsWith(s"$baseUrl/$page"))
  }

  And("^the logged out user enters invalid credentials") {
    val userNameElement = webDriver.findElement(By.cssSelector("[name='username']"))
    val passwordElement = webDriver.findElement(By.cssSelector("[name='password']"))

    userNameElement.sendKeys("dgfhfdgjhgfj")
    passwordElement.sendKeys("fdghfdgh")
  }

  Then("^the logged out user will remain on the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl
      Assert.assertTrue(currentUrl.startsWith(s"$authUrl/$page"))
  }

  And("^the user will see an error message") {
    val errorElement = webDriver.findElement(By.cssSelector("#error-details"))
    Assert.assertNotNull(errorElement)
    Assert.assertEquals("Invalid username or password.", errorElement.getText)
  }

  When("^the logged in user navigates to another website") {
    webDriver.get(s"$otherPageUrl")
  }

  And("^the logged in user navigates back to TDR Home Page") {
    webDriver.get(s"$baseUrl")
  }

  And("^the logged in user clicks the (.*) element$") {
    selector: String =>
      val clickableElement = webDriver.findElement(By.cssSelector(selector))
      clickableElement.click()
  }
}
