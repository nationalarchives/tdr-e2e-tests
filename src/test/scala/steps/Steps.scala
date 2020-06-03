package steps

import java.util

import com.typesafe.config.ConfigFactory
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.steps.StepsUtility
import helpers.users.{KeycloakClient, RandomUtility, UserCredentials}
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.Matchers
import org.openqa.selenium.support.ui.Select

class Steps extends ScalaDsl with EN with Matchers {
  var webDriver: WebDriver = _
  var userId: String = ""

  val configuration = ConfigFactory.load()
  val baseUrl: String = configuration.getString("tdr.base.url")
  val authUrl: String = configuration.getString("tdr.auth.url")
  val userName: String = RandomUtility.randomString()
  val password: String = RandomUtility.randomString(10)
  val nonTdrPageUrl: String = configuration.getString("redirect.base.url")
  val userCredentials: UserCredentials = UserCredentials(userName, password)

  Before() { scenario =>
    webDriver = new ChromeDriver(StepsUtility.getChromeOptions)
  }

  After() { scenario =>
    webDriver.quit()
    KeycloakClient.deleteUser(userId)
  }

  Given("^A logged out user") {
    userId = KeycloakClient.createUser(userCredentials)
  }

  Given("^A logged in user") {
    userId = KeycloakClient.createUser(userCredentials)
    webDriver.get(s"$baseUrl")
    val startElement = webDriver.findElement(By.cssSelector(".govuk-button--start"))
    startElement.click()
    StepsUtility.userLogin(webDriver, userCredentials)
  }

  When("^the user navigates to TDR Home Page") {
    webDriver.get(s"$baseUrl")
  }

  Then("^the logged out user enters valid credentials") {
    StepsUtility.enterUserCredentials(webDriver, userCredentials)
  }

  And("^the logged out user enters invalid credentials") {
    val userNameElement = webDriver.findElement(By.cssSelector("[name='username']"))
    val passwordElement = webDriver.findElement(By.cssSelector("[name='password']"))

    userNameElement.sendKeys("dgfhfdgjhgfj")
    passwordElement.sendKeys("fdghfdgh")
  }

  When("^the logged in user navigates to the (.*) page") {
    page: String =>
      webDriver.get(s"$baseUrl/$page")
  }

  And("^the user clicks the (.*) element$") {
    selector: String =>
      val clickableElement = webDriver.findElement(By.cssSelector(selector))
      clickableElement.click()
  }

  Then("^the logged out user should be at the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl

      Assert.assertTrue(currentUrl.startsWith(s"XYZ"))
  }

  Then("^the user should be at the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl

      Assert.assertTrue(s"actual: $currentUrl, expected: $page", currentUrl.startsWith(s"$baseUrl/$page") || currentUrl.endsWith(page))
  }

  Then("^the user will remain on the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl
      Assert.assertTrue(currentUrl.startsWith(s"$authUrl/$page"))
  }

  And("^the user will see the error message (.*)") {
    errorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector("#error-details"))
      Assert.assertNotNull(errorElement)
      Assert.assertEquals(errorMessage, errorElement.getText)
  }

  And("^the user will see a form error message (.*)") {
    formErrorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector(".govuk-error-message"))
      Assert.assertNotNull(errorElement)
      Assert.assertEquals(s"Error:\n" + formErrorMessage, errorElement.getText)
  }

  Then("^the logged in user should stay at the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl
      Assert.assertTrue(currentUrl.startsWith(s"$baseUrl/$page"))
  }

  And("^the logged in user selects the series (.*)") {
    selectedSeries: String =>
      val seriesDropdown = new Select(webDriver.findElement(By.name("series")))
      seriesDropdown.selectByVisibleText(selectedSeries)
  }

  And("^the user clicks the continue button") {
    val button = webDriver.findElement(By.cssSelector("[type='submit']"))
    button.click()
  }
}
