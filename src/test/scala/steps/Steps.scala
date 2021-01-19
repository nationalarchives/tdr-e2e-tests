package steps

import java.nio.file.Paths
import java.time.Duration
import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory}
import cucumber.api.scala.{EN, ScalaDsl}
import helpers.aws.AWSUtility
import helpers.drivers.DriverUtility._
import helpers.graphql.GraphqlUtility
import helpers.keycloak.{KeycloakClient, UserCredentials}
import helpers.steps.StepsUtility
import helpers.users.RandomUtility
import org.junit.Assert
import org.openqa.selenium.support.ui.{FluentWait, Select, WebDriverWait}
import org.openqa.selenium.{By, JavascriptExecutor, StaleElementReferenceException, WebDriver, WebElement}
import org.scalatest.Matchers

import scala.jdk.CollectionConverters._

class Steps extends ScalaDsl with EN with Matchers {
  var webDriver: WebDriver = _
  var userId: String = ""
  var differentUserId: String = ""
  var consignmentId: UUID = _
  var createdFiles: List[UUID] = _
  var filesWithoutAVMetadata: List[UUID] = _
  var filesWithoutFFIDMetadata: List[UUID] = _
  var filesWithoutChecksumMetadata: List[UUID] = _

  val configuration: Config = ConfigFactory.load()
  val baseUrl: String = configuration.getString("tdr.base.url")
  val authUrl: String = configuration.getString("tdr.auth.url")
  val userName: String = RandomUtility.randomString()
  val differentUserName: String = RandomUtility.randomString()
  val password: String = RandomUtility.randomString(10)
  val differentPassword: String = RandomUtility.randomString(10)
  val userCredentials: UserCredentials = UserCredentials(userName, password)
  val differentUserCredentials: UserCredentials = UserCredentials(differentUserName, differentPassword)

  Before() { scenario =>
    webDriver = initDriver
  }

  After() { scenario =>
    webDriver.quit()
    userCleanUp
  }

  private def login(userCredentials: UserCredentials): Unit = {
    webDriver.get(s"$baseUrl")
    val startElement = webDriver.findElement(By.cssSelector(".govuk-button--start"))
    startElement.click()
    StepsUtility.userLogin(webDriver, userCredentials)
  }

  private def loadPage(page: String): Unit = {
    val pageWithConsignment = page match {
      case "dashboard" | "series" | "some-page" => s"$baseUrl/$page"
      case _ => s"$baseUrl/consignment/$consignmentId/${page.toLowerCase.replaceAll(" ", "-")}"
    }
    webDriver.get(pageWithConsignment)
  }

  private def userCleanUp(): Unit = {
    KeycloakClient.deleteUser(userId)

    //Not all scenarios create the different user
    if (!differentUserId.isEmpty) {
      KeycloakClient.deleteUser(differentUserId)
    }
  }

  Given("^A logged out user") {
    userId = KeycloakClient.createUser(userCredentials)
  }

  Given("^A logged in user who is a member of (.*) transferring body") {
    body: String =>
      userId = KeycloakClient.createUser(userCredentials, Some(body))
      login(userCredentials)
  }

  Given("^A logged in user who is not a member of a transferring body") {
    userId = KeycloakClient.createUser(userCredentials, Option.empty)
    login(userCredentials)
  }

  Given("^A logged in user") {
    userId = KeycloakClient.createUser(userCredentials)
    login(userCredentials)
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

  Then("^the logged out user should be on the login page") {
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

  And("^the transfer export will be complete") {
    val fluentWait = new FluentWait[WebDriver](webDriver)
      .withTimeout(Duration.ofSeconds(600))
      .pollingEvery(Duration.ofSeconds(10))
    val foundExport: Boolean = fluentWait.until(_ => {
      val awsUtility = AWSUtility()
      awsUtility.isFileInS3(configuration.getString("s3.bucket.export"), s"$consignmentId.tar.gz")
    })
    Assert.assertTrue(foundExport)
  }

  Then("^the user will be on a page with the title \"(.*)\"") {
    page: String =>
      new WebDriverWait(webDriver, 120)
        .ignoring(classOf[StaleElementReferenceException])
        /*Ignore stale references exceptions.
        These seem to happen when Selenium selects an element which then disappears when the user is redirected to the next page,
        such as from the upload page to the file checks page. In this case, we only want to check the element on the second page,
        so it doesn't matter if the same element on the first page has disappeared.*/
        .until((driver: WebDriver) => {
          val pageTitle: String = webDriver.findElement(By.className("govuk-heading-xl")).getText
          page == pageTitle
        })
  }

  Then("^the user will be on a page with a panel titled \"(.*)\"") {
    panelTitle: String =>
      new WebDriverWait(webDriver, 120)
        .ignoring(classOf[StaleElementReferenceException])
        /*Ignore stale references exceptions.
        These seem to happen when Selenium selects an element which then disappears when the user is redirected to the next page,
        such as from the upload page to the file checks page. In this case, we only want to check the element on the second page,
        so it doesn't matter if the same element on the first page has disappeared.*/
        .until((driver: WebDriver) => {
          val panel = webDriver.findElement(By.className("govuk-panel__title")).getText
          panel == panelTitle
        })
  }

  Then("^the user should see a general service error \"(.*)\"") {
    errorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector(".govuk-heading-l"))
      Assert.assertNotNull(errorElement)

      Assert.assertTrue(errorElement.getText.contains(errorMessage))
  }

  And("^the user will see the error message (.*)") {
    errorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector(".govuk-error-summary__list"))
      Assert.assertNotNull(errorElement)
      Assert.assertEquals(errorMessage, errorElement.getText)
  }

  And("^the user will see a form error message \"(.*)\"") {
    formErrorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector(".govuk-error-message"))
      Assert.assertNotNull(errorElement)
      Assert.assertEquals(s"Error:\n" + formErrorMessage, errorElement.getText)
  }

  And("^the user will see a summary error message \"(.*)\"") {
    summaryErrorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector(".govuk-error-summary__list a"))
      Assert.assertNotNull(errorElement)
      Assert.assertEquals(summaryErrorMessage, errorElement.getText)
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

  When("^the user selects yes for all checks except \"The records are all English\"") {
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecordtrue"))
    val recordsAllCrownCopyright = webDriver.findElement(By.id("crownCopyrighttrue"))
    recordsAllPublicRecords.click()
    recordsAllCrownCopyright.click()
  }

  When("^the user selects yes to all transfer agreement checks") {
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecordtrue"))
    val recordsAllCrownCopyright = webDriver.findElement(By.id("crownCopyrighttrue"))
    val recordsAllEnglish = webDriver.findElement(By.id("englishtrue"))
    recordsAllPublicRecords.click()
    recordsAllCrownCopyright.click()
    recordsAllEnglish.click()
  }

  When("^the user selects yes to all transfer summary checks") {
    val openRecords = webDriver.findElement(By.id("openRecords"))
    val transferLegalOwnership = webDriver.findElement(By.id("transferLegalOwnership"))
    openRecords.click()
    transferLegalOwnership.click()
  }

  And("^the user confirms all the records are open") {
    val openRecords = webDriver.findElement(By.id("openRecords"))
    openRecords.click()
  }

  And("^the user confirms that DRO has signed off on the records") {
    val droAppraisalAndSelection = webDriver.findElement(By.id("droAppraisalSelection"))
    val droSensitivity = webDriver.findElement(By.id("droSensitivity"))
    droAppraisalAndSelection.click()
    droSensitivity.click()
  }

  And("^the user does not confirm DRO sign off for the records") {
    val droSensitivity = webDriver.findElement(By.id("droSensitivity"))
    droSensitivity.click()
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

  And("^the records checks are complete") {
    val client = GraphqlUtility(userCredentials)
    val createdFiles: List[UUID] = client.createFiles(consignmentId, 1, "E2E TEST UPLOAD FOLDER")
    createdFiles.foreach({
      id =>
        client.createClientsideMetadata(userCredentials, id, "checksumValue", 0)
        client.createAVMetadata(id)
        client.createBackendChecksumMetadata(id)
        client.createFfidMetadata(id)
    })
  }

  And("^an existing upload of (\\d+) files") {
    val client = GraphqlUtility(userCredentials)
    numberOfFiles: Int => {
      createdFiles = client.createFiles(consignmentId, numberOfFiles, "E2E TEST UPLOAD FOLDER")
      //  checksumValue will be replaced with actual checksum soon
      val files = List("testfile1", "testfile2")

      createdFiles.zipWithIndex.foreach {
        case (id, idx) =>
          client.createClientsideMetadata(userCredentials, id, "checksumValue", idx)
          val path = Paths.get(s"${System.getProperty("user.dir")}/src/test/resources/testfiles/${files(idx % 2)}")
          val awsUtility = AWSUtility()
          awsUtility.uploadFileToS3(configuration.getString("s3.bucket.upload"), s"$consignmentId/$id", path)
      }
    }
  }

  And("^(\\d+) of the (.*) scans have finished") {
    val client = GraphqlUtility(userCredentials)
    (filesToProcess: Int, metadataType: String) => {
      val fileRangeToProcess = createdFiles.slice(0, filesToProcess)
      metadataType match {
        case "antivirus" =>
          fileRangeToProcess.foreach(id => client.createAVMetadata(id))
          filesWithoutAVMetadata = createdFiles.drop(filesToProcess)
        case "FFID" =>
          fileRangeToProcess.foreach(id => client.createFfidMetadata(id))
          filesWithoutFFIDMetadata = createdFiles.drop(filesToProcess)
        case "checksum" =>
          fileRangeToProcess.foreach(id => client.createBackendChecksumMetadata(id))
          filesWithoutChecksumMetadata = createdFiles.drop(filesToProcess)
      }
    }
  }

  And("^the user waits for the checks to complete") {
    val client = GraphqlUtility(userCredentials)
    (filesWithoutChecksumMetadata ++ filesWithoutFFIDMetadata ++ filesWithoutAVMetadata).foreach {
      id =>
        client.createAVMetadata(id)
        client.createBackendChecksumMetadata(id)
        client.createFfidMetadata(id)
    }
  }

  When("^the user selects directory containing: (.*)") {
    fileName: String => {
      new WebDriverWait(webDriver, 10).until((driver: WebDriver) => {
        val executor = driver.asInstanceOf[JavascriptExecutor]
        executor.executeScript("return AWS.config && AWS.config.credentials && AWS.config.credentials.accessKeyId") != null
      })

      val input: WebElement = webDriver.findElement(By.cssSelector("#file-selection"))
      input.sendKeys(s"${System.getProperty("user.dir")}/src/test/resources/testfiles/$fileName")
      webDriver.asInstanceOf[JavascriptExecutor].executeScript(s"Object.defineProperty(document.querySelector('#file-selection').files[0], 'webkitRelativePath', {value: 'testfiles/$fileName'})")
    }
  }

  Then("^the (.*) should be visible") {
    targetIdName: String => {
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
        val isNotVisible = StepsUtility.elementHasAttributeHidden(id, webDriver)
        isNotVisible
      })
    }
  }

  And("^the (.*) should have (.*)% progress") {
    (targetIdName: String, percentageProgress: String) => {
      val id: String = targetIdName.replaceAll(" ", "-")
      val progressValue: String = webDriver.findElement(By.id(id)).getAttribute("value")
      Assert.assertTrue(percentageProgress == progressValue)
    }
  }

  And("^the user clicks the (.*) link") {
    linkToClick: String =>
      webDriver.findElement(By.linkText(linkToClick)).click()
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

  And("^a user who did not create the consignment") {
    differentUserId = KeycloakClient.createUser(differentUserCredentials)
  }

  And("^the user who did not create the consignment is logged in on the (.*) page") {
    page: String =>
      loadPage(page)
      StepsUtility.userLogin(webDriver, differentUserCredentials)
  }

  Then("^the user who did not create the consignment will see the error message \"(.*)\"") {
    errorMessage: String =>
      val errorElement = webDriver.findElement(By.cssSelector(".govuk-heading-l"))
      Assert.assertNotNull(errorElement)

      Assert.assertTrue(errorElement.getText.contains(errorMessage))
  }

  And("^the logged out user attempts to access the (.*) page") {
    page: String =>
      loadPage(page)
  }

  And("^the user navigates to a page that does not exist") {
    loadPage("some-page")
  }
}
