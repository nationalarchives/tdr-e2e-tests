package steps

import com.typesafe.config.{Config, ConfigFactory}
import helpers.aws.AWSUtility
import helpers.drivers.DriverUtility._
import helpers.graphql.GraphqlUtility
import helpers.graphql.GraphqlUtility.MatchIdInfo
import helpers.keycloak.{KeycloakClient, UserCredentials}
import helpers.logging.AssertionErrorMessages._
import helpers.steps.StepsUtility
import helpers.steps.StepsUtility.calculateTestFileChecksum
import helpers.users.RandomUtility
import io.cucumber.scala.{EN, ScalaDsl}
import org.junit.Assert
import org.openqa.selenium._
import org.openqa.selenium.devtools.HasDevTools
import org.openqa.selenium.devtools.v85.inspector.Inspector
import org.openqa.selenium.devtools.v85.network.Network
import org.openqa.selenium.devtools.v85.network.model.{RequestWillBeSent, ResponseReceived}
import org.openqa.selenium.devtools.v85.target.Target
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.{FluentWait, Select, WebDriverWait}
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths
import java.time.Duration
import java.util
import java.util.UUID
import java.util.function.Consumer
import scala.jdk.CollectionConverters._
import scala.jdk.FunctionWrappers.AsJavaConsumer
import scala.jdk.OptionConverters.RichOption

class Steps extends ScalaDsl with EN with Matchers {

  var webDriver: WebDriver = _
  var userId: String = ""
  var differentUserId: String = ""
  var consignmentId: UUID = _
  var createdFiles: List[UUID] = _
  var createdFilesIdToChecksum: Map[UUID, String] = Map()
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
  val checksumValue = "checksum"

  Before {
    webDriver = initDriver

    val devTools = webDriver.asInstanceOf[FirefoxDriver].getDevTools
    devTools.createSessionIfThereIsNotOne()
    val targets = devTools.send(Target.getTargets)
    devTools.send(Target.attachToTarget(targets.get(0).getTargetId, None.toJava))
    devTools.send(Network.enable(None.toJava, None.toJava, None.toJava))
    val b: Consumer[RequestWillBeSent] = AsJavaConsumer[RequestWillBeSent](entry => {
      println(s"Response (Req id) URL : (${entry.getRequestId}) ${entry.getRequest.getUrl} (${entry.getRequest.getMethod})")
    })
//    devTools.addListener(Network.requestWillBeSent(), b)
//
//    devTools.addListener(Network.responseReceived(), AsJavaConsumer[ResponseReceived](entry => {
//      s"Response (Req id) URL : (${entry.getRequestId}) ${entry.getResponse.getUrl} (${entry.getResponse.getStatus})"
//    }))
    ()
  }

  After {
    webDriver.quit()
    userCleanUp()
  }

  implicit class JavaWebElementList(javaList: util.List[WebElement]) {
    def toScalaList: List[WebElement] = javaList.asScala.toList
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
    if (differentUserId.nonEmpty) {
      KeycloakClient.deleteUser(differentUserId)
    }
  }

  private def findFormErrorMessageOnPage(formType: String, genericErrorMessage: String = "", errorClassName: String): Unit = {
    val formErrorMessages: Seq[String] = formType match {
      case "Final Transfer Confirmation" =>
        Seq("All records must be confirmed as open before proceeding",
          "Transferral of legal ownership of all records must be confirmed before proceeding")
    }
    val errorElements: util.List[WebElement] = webDriver.findElements(By.cssSelector(errorClassName))
    Assert.assertNotNull(elementMissingMessage(errorClassName), errorElements)

    for (i <- formErrorMessages.indices) {
      Assert.assertEquals("Incorrect error message", genericErrorMessage + formErrorMessages(i), errorElements.get(i).getText)
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

  Then("^the (.*) button is not displayed on the page") {
    button: String =>
      webDriver.findElements(By.linkText(button)).isEmpty
  }

  Then("^the logged out user should be on the login page") {
    val currentUrl: String = webDriver.getCurrentUrl
    Assert.assertTrue(doesNotMatchExpected(currentUrl, "login"), currentUrl.startsWith(s"$authUrl/auth"))
  }

  Then("^the user will remain on the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl
      val url = if (page == "auth") authUrl else baseUrl
      Assert.assertTrue(doesNotMatchExpected(currentUrl, page), currentUrl.startsWith(s"$url/$page"))
  }

  Then("^the user should be on the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl

      Assert.assertTrue(doesNotMatchExpected(currentUrl, page), currentUrl.startsWith(s"$baseUrl/$page") || currentUrl.endsWith(page))
  }

  And("^the transfer export will be complete") {
    val client = GraphqlUtility(userCredentials)
    val consignmentRef = client.getConsignmentExport(consignmentId).get.getConsignment.get.consignmentReference

    val fluentWait = new FluentWait[WebDriver](webDriver)
      .withTimeout(Duration.ofSeconds(600))
      .pollingEvery(Duration.ofSeconds(10))

    val foundExport: Boolean = fluentWait.until(_ => {
      val awsUtility = AWSUtility()
      awsUtility.isFileInS3(configuration.getString("s3.bucket.export"), s"$consignmentRef.tar.gz")
    })
    Assert.assertTrue("No export found", foundExport)
  }

  Then("^the user will be on a page with the title \"(.*)\"") {
    page: String =>
      StepsUtility.waitForElementTitle(webDriver, page, "govuk-heading-l")
  }

  Then("^the user will be on a page with a panel titled \"(.*)\"") {
    panelTitle: String =>
      StepsUtility.waitForElementTitle(webDriver, panelTitle, "govuk-panel__title")
  }

  Then("^the user will be on a page with a banner titled \"(.*)\"") {
    panelTitle: String =>
      StepsUtility.waitForElementTitle(webDriver, panelTitle, "success-summary__title")
  }

  Then("^the user should see a general service error \"(.*)\"") {
    errorMessage: String =>
      val selector = ".govuk-heading-l"
      val errorElement = webDriver.findElement(By.cssSelector(selector))
      Assert.assertNotNull(elementMissingMessage(selector), errorElement)

      val actualErrorText = errorElement.getText
      Assert.assertTrue(doesNotContain(actualErrorText, errorMessage), actualErrorText.contains(errorMessage))
  }

  And("^the user will see the error message (.*)") {
    errorMessage: String =>
      val selector = ".govuk-error-summary__list"
      val errorElement = webDriver.findElement(By.cssSelector(selector))
      Assert.assertNotNull(elementMissingMessage(selector), errorElement)
      Assert.assertEquals(errorMessage, errorElement.getText)
  }

  And("^the user will see the error summary \"(.*)\"") {
    errorMessage: String =>
      val selector = ".govuk-error-summary__body"
      val errorElement = webDriver.findElement(By.cssSelector(selector))
      Assert.assertNotNull(elementMissingMessage(selector), errorElement)
      Assert.assertTrue(errorElement.getText.contains(errorMessage))
  }

  And("^the user will see a form error message \"(.*)\"") {
    formErrorMessage: String =>
      val selector = ".govuk-error-message"
      val errorElement = webDriver.findElement(By.cssSelector(selector))
      Assert.assertNotNull(elementMissingMessage(selector), errorElement)
      Assert.assertEquals(s"Error:\n" + formErrorMessage, errorElement.getText)
  }

  And("^the user will see all of the (.*) form's error messages") {
    formType: String =>
      findFormErrorMessageOnPage(formType, genericErrorMessage = "Error:\n", errorClassName = ".govuk-error-message")
  }

  And("^the user will see a summary error message \"(.*)\"") {
    summaryErrorMessage: String =>
      val selector = ".govuk-error-summary__list a"
      val errorElement = webDriver.findElement(By.cssSelector(selector))
      Assert.assertNotNull(elementMissingMessage(selector), errorElement)
      Assert.assertEquals(summaryErrorMessage, errorElement.getText)
  }

  And("^the user will see all of the (.*) summary error messages") {
    formType: String =>
      findFormErrorMessageOnPage(formType, errorClassName = ".govuk-error-summary__list a")
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
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecord"))
    val recordsAllCrownCopyright = webDriver.findElement(By.id("crownCopyright"))
    recordsAllPublicRecords.click()
    recordsAllCrownCopyright.click()
  }

  When("^the user selects yes to all transfer agreement checks") {
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecord"))
    val recordsAllCrownCopyright = webDriver.findElement(By.id("crownCopyright"))
    val recordsAllEnglish = webDriver.findElement(By.id("english"))
    recordsAllPublicRecords.click()
    recordsAllCrownCopyright.click()
    recordsAllEnglish.click()
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
    val files = List("testfile1", "testfile2")
    val checksumWithIndex: List[MatchIdInfo] = files.zipWithIndex.map({
      case (fileName, idx) =>
        val path = Paths.get(s"${System.getProperty("user.dir")}/src/test/resources/testfiles/$fileName")
        val checksumValue = calculateTestFileChecksum(path)
        MatchIdInfo(checksumValue, path, idx)
    })
    val fileAndMatchIds = client.addFilesAndMetadata(consignmentId, "E2E TEST UPLOAD FOLDER", checksumWithIndex)
    fileAndMatchIds.foreach(fm => {
      val checksum = checksumWithIndex.find(_.matchId == fm.matchId).map(_.checksum)
      client.createAVMetadata(fm.fileId)
      client.createBackendChecksumMetadata(fm.fileId, checksum)
      client.createFfidMetadata(fm.fileId)
    })
  }

  And("^the checksum check has failed") {
    val client = GraphqlUtility(userCredentials)
    val matchIdInfo = List(MatchIdInfo(checksumValue, Paths.get("."), 0))
    val id = client.addFilesAndMetadata(consignmentId, "E2E TEST UPLOAD FOLDER", matchIdInfo).map(_.fileId).head

    client.createAVMetadata(id)
    client.createBackendChecksumMetadata(id, Option("mismatchedchecksumvalue"))
    client.createFfidMetadata(id)
  }

  And("^the antivirus check has failed") {
    val client = GraphqlUtility(userCredentials)

    val matchIdInfo = List(MatchIdInfo(checksumValue, Paths.get("."), 0))
    val id: UUID = client.addFilesAndMetadata(consignmentId, "E2E TEST UPLOAD FOLDER", matchIdInfo).map(_.fileId).head
    client.createAVMetadata(id, "antivirus failed")
    client.createBackendChecksumMetadata(id, Some(checksumValue))
    client.createFfidMetadata(id)
  }

  And("^the FFID \"(.*)\" check has failed") {
    (checkName: String) => {
      val passwordProtectedPuid = "fmt/494"
      val zipFilePuid = "fmt/289"
      val client = GraphqlUtility(userCredentials)
      val matchIdInfo = List(MatchIdInfo(checksumValue, Paths.get("."), 0))
      val id: UUID = client.addFilesAndMetadata(consignmentId, "E2E TEST UPLOAD FOLDER", matchIdInfo).map(_.fileId).head
      client.createAVMetadata(id)
      client.createBackendChecksumMetadata(id, Some(checksumValue))
      checkName match {
        case "password protected" => client.createFfidMetadata(id, passwordProtectedPuid)
        case "zip file" => client.createFfidMetadata(id, zipFilePuid)
      }
    }
  }

  And("^an existing upload of (\\d+) files") {
    val client = GraphqlUtility(userCredentials)
    numberOfFiles: Int => {
      val files = List("testfile1", "testfile2")

      val matchIdInfo: List[MatchIdInfo] = List.tabulate(numberOfFiles)(n => n).map(idx => {
        val path = Paths.get(s"${System.getProperty("user.dir")}/src/test/resources/testfiles/${files(idx % 2)}")
        val checksumValue = calculateTestFileChecksum(path)
        MatchIdInfo(checksumValue, path, idx)
      })
      val addFilesAndMetadataResult = client.addFilesAndMetadata(consignmentId,  "E2E TEST UPLOAD FOLDER", matchIdInfo)
      createdFiles = addFilesAndMetadataResult.map(_.fileId)

      val awsUtility = AWSUtility()

      createdFilesIdToChecksum = addFilesAndMetadataResult.map(res => {
        val info: MatchIdInfo = matchIdInfo.find(_.matchId == res.matchId).get
        awsUtility.uploadFileToS3(configuration.getString("s3.bucket.upload"), s"$consignmentId/${res.fileId}", info.path)
        res.fileId -> info.checksum
      }).toMap
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
          fileRangeToProcess.foreach(id => client.createBackendChecksumMetadata(id, createdFilesIdToChecksum.get(id)))
          filesWithoutChecksumMetadata = createdFiles.drop(filesToProcess)
      }
    }
  }

  And("^the user waits for the checks to complete") {
    val client = GraphqlUtility(userCredentials)
    (filesWithoutChecksumMetadata ++ filesWithoutFFIDMetadata ++ filesWithoutAVMetadata).foreach {
      id =>
        client.createAVMetadata(id)
        client.createBackendChecksumMetadata(id, createdFilesIdToChecksum.get(id))
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
      new WebDriverWait(webDriver, Duration.ofSeconds(10)).until((driver: WebDriver) => {
        val isVisible = !StepsUtility.elementHasClassHide(id, driver)
        isVisible
      })
    }
  }

  Then("^the (.*) should not be visible") {
    (targetIdName: String) => {
      val id = targetIdName.replaceAll(" ", "-")
      new WebDriverWait(webDriver, Duration.ofSeconds(10)).until((driver: WebDriver) => {
        val isNotVisible = StepsUtility.elementHasAttributeHidden(id, webDriver)
        isNotVisible
      })
    }
  }

  And("^the (.*) button should be disabled") {
    (targetIdName: String) => {
      val id = targetIdName.replaceAll(" ", "-")
      Assert.assertTrue(StepsUtility.elementHasClassDisabled(id, webDriver))
    }
  }

  And("^the (.*) button should be enabled") {
    (targetIdName: String) => {
      val id = targetIdName.replaceAll(" ", "-")
      new WebDriverWait(webDriver, Duration.ofSeconds(25)).ignoring(classOf[AssertionError]).until((driver: WebDriver) => {
        Assert.assertFalse(StepsUtility.elementHasClassDisabled(id, webDriver))
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

  When("^the user clicks their browser's back button") {
    webDriver.navigate().back()
  }

  Then("^the user should see the upload error message \"(.*)\"") {
    errorMessage: String =>
      val selector = ".upload-error__message"
      val errorElement = webDriver.findElement(By.cssSelector(selector))
      Assert.assertNotNull(elementMissingMessage(selector), errorElement)
      val errorElementText = errorElement.getText
      val specificError = errorMessage.replace("{consignmentId}", s"$consignmentId")
      Assert.assertTrue(doesNotContain(errorElementText, specificError), errorElementText.contains(specificError))
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
      val selector = ".govuk-heading-l"
       new WebDriverWait(webDriver, Duration.ofSeconds(10)).ignoring(classOf[AssertionError]).until((driver: WebDriver) => {
         val errorElement = webDriver.findElement(By.cssSelector(selector))
         Assert.assertNotNull(elementMissingMessage(selector), errorElement)

         val errorElementText = errorElement.getText
         Assert.assertTrue(doesNotContain(errorElementText, errorMessage), errorElementText.contains(errorMessage))
      })
  }

  And("^the logged out user attempts to access the (.*) page") {
    page: String =>
      loadPage(page)
  }

  And("^the user navigates to a page that does not exist") {
    loadPage("some-page")
  }

  And("^the user confirms that they are transferring legal ownership of the records to TNA") {
    val transferLegalOwnership = webDriver.findElement(By.id("transferLegalOwnership"))
    transferLegalOwnership.click()
  }

  Then("^the confirm transfer page shows the user that (.*) files have been uploaded") {
    numberOfFilesUploaded: String => {
      val selector = ".govuk-summary-list"
      val summary = webDriver.findElement(By.cssSelector(selector))
      Assert.assertNotNull(elementMissingMessage(selector), summary)

      val summaryText = summary.getText
      val expectedText = s"$numberOfFilesUploaded files uploaded"
      Assert.assertTrue(doesNotContain(summaryText, expectedText), summaryText.contains(expectedText))
    }
  }

  And("^the user sees a transfer confirmation with related information") {
    val expectedKeys: List[String] = List(
      "Series reference",
      "Consignment reference",
      "Transferring body",
      "Files uploaded for transfer"
    )
    val cssSelector = ".govuk-summary-list"
    val confirmTransferElement = webDriver.findElement(By.cssSelector(cssSelector))
    val confirmTransferKeys: List[WebElement] = webDriver.findElements(By.cssSelector(".govuk-summary-list__key")).toScalaList
    val confirmTransferValues: List[WebElement] = webDriver.findElements(By.cssSelector(".govuk-summary-list__value")).toScalaList

    Assert.assertNotNull(elementMissingMessage(cssSelector), confirmTransferElement)
    Assert.assertTrue(confirmTransferKeys.size == 4)
    confirmTransferKeys.foreach(key => {
     val keyText = key.getText
      Assert.assertTrue("Confirm transfer list key empty", keyText.nonEmpty)
      Assert.assertTrue("Confirm transfer list key is incorrect",expectedKeys.contains(keyText))
    })

    Assert.assertTrue(confirmTransferValues.size == 4)
    confirmTransferValues.foreach(value => {
      Assert.assertTrue("Confirm transfer list value empty", value.getText.nonEmpty)
    })
  }

  Then("^the user will see the message \"(.*)\"") {
    pageMessage: String => {
      val selector = s"//p[contains(text(), '$pageMessage')]"
      val message = webDriver.findElement(By.xpath(selector))
      Assert.assertNotNull(elementMissingMessage(selector), message)

      val summaryText = message.getText
      val expectedText = pageMessage
      Assert.assertTrue(doesNotContain(summaryText, expectedText), summaryText.contains(expectedText))
    }
  }
}
