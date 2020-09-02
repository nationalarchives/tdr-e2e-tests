package steps

import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory}
import cucumber.api.scala.{EN, ScalaDsl}
import graphql.codegen.AddFiles.addFiles
import helpers.drivers.DriverUtility._
import helpers.graphql.GraphqlUtility
import helpers.keycloak.{KeycloakClient, UserCredentials}
import helpers.steps.StepsUtility
import helpers.users.RandomUtility
import org.junit.Assert
import org.openqa.selenium.support.ui.{Select, WebDriverWait}
import org.openqa.selenium.{By, JavascriptExecutor, WebDriver}
import org.scalatest.Matchers
import uk.gov.nationalarchives.tdr.GraphQlResponse

import scala.jdk.CollectionConverters._

class Steps extends ScalaDsl with EN with Matchers {
  var webDriver: WebDriver = _
  var userId: String = ""
  var consignmentId: UUID = _

  val configuration: Config = ConfigFactory.load()
  val baseUrl: String = configuration.getString("tdr.base.url")
  val authUrl: String = configuration.getString("tdr.auth.url")
  val userName: String = RandomUtility.randomString()
  val password: String = RandomUtility.randomString(10)
  val userCredentials: UserCredentials = UserCredentials(userName, password)

  Before() { scenario =>
    webDriver = initDriver
  }

  After() { scenario =>
    webDriver.quit()
    KeycloakClient.deleteUser(userId)
  }

  private def login(): Unit = {
    webDriver.get(s"$baseUrl")
    val startElement = webDriver.findElement(By.cssSelector(".govuk-button--start"))
    startElement.click()
    StepsUtility.userLogin(webDriver, userCredentials)
  }

  private def loadPage(page: String): Unit = {
    val pageWithConsignment = page match {
      case "series" => s"$baseUrl/$page"
      case _ => s"$baseUrl/consignment/$consignmentId/${page.toLowerCase.replaceAll(" ", "-")}"
    }
    webDriver.get(pageWithConsignment)
  }

  Given("^A logged out user") {
    userId = KeycloakClient.createUser(userCredentials)
  }

  Given("^A logged in user who is a member of (.*) transferring body") {
    body: String =>
      userId = KeycloakClient.createUser(userCredentials, Some(body))
      login()
  }

  Given("^A logged in user who is not a member of a transferring body") {
    userId = KeycloakClient.createUser(userCredentials, Option.empty)
    login()
  }

  Given("^A logged in user") {
    userId = KeycloakClient.createUser(userCredentials)
    login()
  }

  And("^the user is logged in on the (.*) page") {
    page: String =>
      loadPage(page)
      StepsUtility.userLogin(webDriver, userCredentials)
  }

  When("^the user navigates to TDR Home Page") {
    webDriver.get(s"$baseUrl")
  }

  And("^the logged out user enters valid credentials") {
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
      loadPage(page)
  }

  And("^the (.*) page is loaded") {
    page: String =>
      loadPage(page)
  }

  And("^the user clicks on the (.*) button") {
    button: String =>
      webDriver.findElement(By.linkText(button)).click()

  }

  Then("^the logged out user should be on the auth page") {
      val currentUrl: String = webDriver.getCurrentUrl
      Assert.assertTrue(currentUrl.startsWith(s"$authUrl/auth"))
  }

  Then("^the user will remain on the (.*) page") {
    page: String =>
    val currentUrl: String = webDriver.getCurrentUrl
    val url = if (page == "auth") authUrl else baseUrl
    Assert.assertTrue(currentUrl.startsWith(s"$url/$page"))
  }

  Then("^the user should be on the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl

      Assert.assertTrue(s"actual: $currentUrl, expected: $page", currentUrl.startsWith(s"$baseUrl/$page") || currentUrl.endsWith(page))
  }

  Then("^the user will be on a page with the title \"(.*)\"") {
    page: String =>
      new WebDriverWait(webDriver, 10).until((driver: WebDriver) => {
        val pageTitle: String = webDriver.findElement(By.className("govuk-heading-xl")).getText
        page == pageTitle
      })
  }

  Then("^the user should see a user-specific general error (.*)") {
    errorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector("#general-error"))
      Assert.assertNotNull(errorElement)
      val specificError = errorMessage.replace("{userId}", s"Some($userId)")

      Assert.assertTrue(errorElement.getText.contains(specificError))
  }

  And("^the user will see the error message (.*)") {
    errorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector("#error-details"))
      Assert.assertNotNull(errorElement)
      Assert.assertEquals(errorMessage, errorElement.getText)
  }

  And("^the user will see a form error message \"(.*)\"") {
    formErrorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector(".govuk-error-message"))
      Assert.assertNotNull(errorElement)
      Assert.assertEquals(s"Error:\n" + formErrorMessage, errorElement.getText)
  }

  Then("^the user should see the series dropdown values (.*)") {
    expectedValues: String =>
      val seriesList: List[String] = expectedValues.split(",").toList
      val seriesDropdown = new Select(webDriver.findElement(By.name("series")))
      val seriesText: List[String] = seriesDropdown.getOptions.asScala.map(_.getText).toList.tail

      Assert.assertEquals(seriesList, seriesText)
  }

  Then("^the user should see an empty series dropdown") {
      val seriesDropdown = new Select(webDriver.findElement(By.name("series")))
      val seriesText: List[String] = seriesDropdown.getOptions.asScala.map(_.getText).toList.tail
      Assert.assertTrue(seriesText.isEmpty)
  }

  And("^the user selects the series (.*)") {
    selectedSeries: String =>
      val seriesDropdown = new Select(webDriver.findElement(By.name("series")))
      seriesDropdown.selectByVisibleText(selectedSeries)
  }

  And("^the user clicks the continue button") {
    val button = webDriver.findElement(By.cssSelector("[type='submit']"))
    button.click()
  }

  When("^the user selects yes for all checks except \"The records are all Digital\"") {
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecordtrue"))
    val recordsAllCrownCopyright = webDriver.findElement(By.id("crownCopyrighttrue"))
    val recordsAllEnglish = webDriver.findElement(By.id("englishtrue"))
    recordsAllPublicRecords.click()
    recordsAllCrownCopyright.click()
    recordsAllEnglish.click()
  }

  When("^the user selects yes to all transfer agreement checks") {
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecordtrue"))
    val recordsAllCrownCopyright = webDriver.findElement(By.id("crownCopyrighttrue"))
    val recordsAllEnglish = webDriver.findElement(By.id("englishtrue"))
    val recordsAllDigital = webDriver.findElement(By.id("digitaltrue"))
    recordsAllPublicRecords.click()
    recordsAllCrownCopyright.click()
    recordsAllEnglish.click()
    recordsAllDigital.click()
  }

  And("^the user confirms that DRO has signed off on the records") {
    val droAppraisalAndSelection = webDriver.findElement(By.id("droAppraisalSelection"))
    val droSensitivityAndOpen = webDriver.findElement(By.id("droSensitivity"))
    droAppraisalAndSelection.click()
    droSensitivityAndOpen.click()
  }

  And("^the user does not confirm DRO sign off for the records") {
    val droSensitivityAndOpen = webDriver.findElement(By.id("droSensitivity"))
    droSensitivityAndOpen.click()
  }

  And("^an existing consignment for transferring body (.*)") {
    body: String =>
      val client = GraphqlUtility(userCredentials)
      consignmentId = client.createConsignment(body).get.addConsignment.consignmentid.get
  }

  And("^an existing transfer agreement") {
    val client = GraphqlUtility(userCredentials)
    client.createTransferAgreement(consignmentId)
  }

  And("^an existing upload") {
//  with this step, we can add the associated metadata processing as they are added to the front-end project (AVMetadata, Checksum, FileFormat).
//  75% progress has been chosen for the AVMetadata progress as this is a more realistic test than using 100% or 0%.
    val client = GraphqlUtility(userCredentials)
    val createdFiles: List[UUID] = client.createFiles(consignmentId)
    createdFiles.drop(1).foreach(id => client.createAVMetadata(id))
  }

  When("^the user selects directory containing: (.*)") {
    (fileName: String) => {
      new WebDriverWait(webDriver, 10).until((driver: WebDriver) => {
        val executor = driver.asInstanceOf[JavascriptExecutor]
        executor.executeScript("return AWS.config && AWS.config.credentials && AWS.config.credentials.accessKeyId") != null
      })

      val input = webDriver.findElement(By.cssSelector("#file-selection"))
      input.sendKeys(s"${System.getProperty("user.dir")}/src/test/resources/testfiles/${fileName}")
    }
  }

  Then("^the (.*) should be visible") {
    (targetIdName: String) => {
      val id = targetIdName.replaceAll(" ", "-")
      new WebDriverWait(webDriver, 10).until((driver: WebDriver) => {
        val isVisible = !StepsUtility.elementHasClassHide(id, driver)
        isVisible
      })
    }
  }

  Then("^the (.*) should not be visible") {
    (targetIdName: String) => {
      val id = targetIdName.replaceAll(" ", "-")
      new WebDriverWait(webDriver, 10).until((driver: WebDriver) => {
        val isNotVisible = StepsUtility.elementHasClassHide(id, webDriver)
        isNotVisible
      })
    }
  }

  And("^the av metadata progress bar should have (.*)% progress") {
    (barProgress: String) => {
      val avProgress: String = webDriver.findElement(By.id("av-metadata-progress-bar")).getAttribute("value")
      Assert.assertTrue(barProgress == avProgress)
    }
  }

  And("^the user clicks the (.*) link") {
    linkClicked: String =>
      webDriver.findElement(By.linkText(linkClicked)).click()
  }

  When("^the user clicks their browser's back button") {
    webDriver.navigate().back()
  }

  Then("^the user should see the upload error message \"(.*)\"") {
    errorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector(".upload-error__message"))
      Assert.assertNotNull(errorElement)
      val specificError = errorMessage.replace("{consignmentId}", s"$consignmentId")
      Assert.assertTrue(errorElement.getText.contains(specificError))
  }
}
