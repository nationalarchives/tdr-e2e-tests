package steps

import com.typesafe.config.{Config, ConfigFactory}
import io.cucumber.scala.{EN, ScalaDsl, Scenario}
import helpers.aws.AWSUtility
import helpers.drivers.DriverUtility._
import helpers.graphql.GraphqlUtility
import helpers.graphql.GraphqlUtility.MatchIdInfo
import helpers.keycloak.{KeycloakClient, UserCredentials}
import helpers.logging.AssertionErrorMessages._
import helpers.steps.StepsUtility
import helpers.steps.StepsUtility.calculateTestFileChecksum
import helpers.users.RandomUtility
import org.junit.Assert
import org.openqa.selenium.support.ui.{FluentWait, Select, WebDriverWait}
import org.openqa.selenium._
import org.scalatest.{Matchers, stats}

import java.io.File
import java.nio.file.Paths
import java.time.Duration
import java.util
import java.util.UUID
import scala.collection.convert.ImplicitConversions.`seq AsJavaList`
import scala.io.Source
import scala.jdk.CollectionConverters._

class Steps extends ScalaDsl with EN with Matchers {
  var webDriver: WebDriver = _
  var userId: String = ""
  var userType: String = ""
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
  val email: String = s"${RandomUtility.randomString()}@testSomething.com"
  val differentEmail: String = s"${RandomUtility.randomString()}@testSomething.com"
  val invalidEmail: String = "dgfhfdgjhgfj"
  val password: String = RandomUtility.randomString(10)
  val differentPassword: String = RandomUtility.randomString(10)
  val invalidPassword: String = "fdghfdgh"
  val userCredentials: UserCredentials = UserCredentials(email, password)
  val differentUserCredentials: UserCredentials = UserCredentials(differentEmail, differentPassword)
  val invalidUserCredentials: UserCredentials = UserCredentials(invalidEmail, invalidPassword)
  val checksumValue = "checksum"

  Before { scenario : Scenario =>
    webDriver = initDriver
  }

  After { scenario : Scenario =>
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
    val isJudgment = userType == "judgment"
    val path = if(isJudgment) "judgment" else "consignment"
    val pageWithConsignment = page match {
      case "homepage" | "some-page" => s"$baseUrl/$page"
      case "faq" | "help" => if(isJudgment) s"$baseUrl/$path/$page" else s"$baseUrl/$page"
      case "Download Metadata" => s"$baseUrl/$path/$consignmentId/additional-metadata/${page.toLowerCase.replaceAll(" ", "-")}"
      case _ => s"$baseUrl/$path/$consignmentId/${page.toLowerCase.replaceAll(" ", "-")}"
    }
    webDriver.get(pageWithConsignment)
  }

  private def userCleanUp(): Unit = {
    this.userType = ""
    KeycloakClient.deleteUser(userId)

    //Not all scenarios create the different user
    if (!differentUserId.isEmpty) {
      KeycloakClient.deleteUser(differentUserId)
    }
  }

  private def findFormErrorMessageOnPage(formType: String, genericErrorMessage: String = "", errorClassName: String): Unit = {
    val formErrorMessages: Seq[String] = formType match {
      case "Final Transfer Confirmation" =>
        Seq("Transferral of legal custody of all records must be confirmed before proceeding")
    }
    val errorElements: util.List[WebElement] = webDriver.findElements(By.cssSelector(errorClassName))
    Assert.assertNotNull(elementMissingMessage(errorClassName), errorElements)

    for (i <- formErrorMessages.indices) {
      Assert.assertEquals("Incorrect error message", genericErrorMessage + formErrorMessages(i), errorElements.get(i).getText)
    }
  }

  private def selectItem(fileName: String): Unit = {
    //Wait for the cookies endpoint to respond. There is no visible change to the page when this happens so we just sleep.
    Thread.sleep(15 * 1000)

    val input: WebElement = webDriver.findElement(By.cssSelector("#file-selection"))
    input.sendKeys(s"${System.getProperty("user.dir")}/src/test/resources/testfiles/$fileName")
    webDriver.asInstanceOf[JavascriptExecutor].executeScript(s"Object.defineProperty(document.querySelector('#file-selection').files[0], 'webkitRelativePath', {value: 'testfiles/$fileName'})")
  }

  Given("^A logged out (.*) user") {
    userType: String =>
      userId = KeycloakClient.createUser(userCredentials, Some("Mock 1 Department"), Some(userType))
      this.userType = userType
  }

  Given("^A logged in (.*) user who is a member of (.*) transferring body") {
    (userType: String, body: String) =>
      userId = KeycloakClient.createUser(userCredentials, Some(body), Some(userType))
      login(userCredentials)
  }

  Given("^A logged in (.*) user who is not a member of a transferring body") {
    userType: String =>
      userId = KeycloakClient.createUser(userCredentials, Option.empty, Some(userType))
      login(userCredentials)
  }

  Given("^A logged in (.*) user") {
    userType: String =>
      userId = KeycloakClient.createUser(userCredentials, Some("Mock 1 Department"), Some(userType))
      login(userCredentials)
      this.userType = userType
  }

  And("^the user is logged in on the (.*) page") {
    page: String =>
      loadPage(page)
      StepsUtility.userLogin(webDriver, userCredentials)
      val pageNameInUrl = page.toLowerCase.replaceAll(" ", "-")

      new WebDriverWait(webDriver, 30).until((driver: WebDriver) => {
        val currentUrl: String = webDriver.getCurrentUrl
        currentUrl.contains(pageNameInUrl)
      })
      val currentUrl: String = webDriver.getCurrentUrl
      Assert.assertTrue(doesNotMatchExpected(currentUrl, s"the url of the $page"), currentUrl.contains(pageNameInUrl))
  }

  When("^the user navigates to TDR Start Page") {
    webDriver.get(s"$baseUrl")
  }

  And("^the logged out user enters valid credentials") {
    StepsUtility.enterUserCredentials(webDriver, userCredentials)
  }

  And("^the logged out user enters invalid credentials") {
    StepsUtility.enterUserCredentials(webDriver, invalidUserCredentials)
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
      new WebDriverWait(webDriver, 30).until(
        (driver: WebDriver) => webDriver.findElement(By.linkText(button)).click()
      )
  }

  Then("^the (.*) button is not displayed on the page") {
    button: String =>
      webDriver.findElements(By.linkText(button)).isEmpty
  }

  Then("^the logged out user should be on the login page") {
    new WebDriverWait(webDriver, 30).until((driver: WebDriver) => {
      val currentUrl: String = webDriver.getCurrentUrl
      currentUrl.startsWith(authUrl)
    })
    val currentUrl: String = webDriver.getCurrentUrl
    Assert.assertTrue(doesNotMatchExpected(currentUrl, "the login url"), currentUrl.startsWith(authUrl))
  }

  Then("^the user will remain on the (.*) page") {
    page: String =>
      val currentUrl: String = webDriver.getCurrentUrl
      val url = if (page == "auth") authUrl else baseUrl
      val expectedPage = if (page == "auth") "realms" else page
      Assert.assertTrue(doesNotMatchExpected(currentUrl, expectedPage), currentUrl.startsWith(s"$url/$expectedPage"))
  }

  Then("^the user should be on the (.*) page") {
    page: String =>
      new WebDriverWait(webDriver, 30).until((driver: WebDriver) => {
        val currentUrl: String = webDriver.getCurrentUrl
        currentUrl.startsWith(s"$baseUrl/$page") || currentUrl.endsWith(page)
      })
      val currentUrl: String = webDriver.getCurrentUrl

      Assert.assertTrue(doesNotMatchExpected(currentUrl, page), currentUrl.startsWith(s"$baseUrl/$page") || currentUrl.endsWith(page))
  }

  Then("^the user should be on a page with (.*) and a consignmentId in the URL") {
    page: String =>
      val nonConsignmentIds = Set("judgment", "consignment", "nationalarchives.gov.uk")
      new WebDriverWait(webDriver, 30).until((driver: WebDriver) => {
        val currentUrl: String = webDriver.getCurrentUrl
        val secondFromLastElementInUrl = currentUrl.split("/").takeRight(2).head
        // Checking that the consignmentId is available in the url. If 2nd from last element does not contain
        // "judgment", "consignment" or "nationalarchives.gov.uk", then it's probably the consignmentId
        !nonConsignmentIds.exists(notConsignmentId => secondFromLastElementInUrl.contains(notConsignmentId))
        })
      val currentUrl: String = webDriver.getCurrentUrl
      val consignmentIdAsString = currentUrl.split("/").takeRight(2).head
      consignmentId = UUID.fromString(consignmentIdAsString)

      Assert.assertTrue(doesNotMatchExpected(currentUrl, page), currentUrl.startsWith(s"$baseUrl/$page") || currentUrl.endsWith(page))
  }

  And("^the (.*) transfer export will be complete") {
    consignmentType: String =>
    val client = GraphqlUtility(userCredentials)
    val consignmentRef = client.getConsignmentExport(consignmentId).get.getConsignment.get.consignmentReference

    val fluentWait = new FluentWait[WebDriver](webDriver)
      .withTimeout(Duration.ofSeconds(600))
      .pollingEvery(Duration.ofSeconds(10))

    val foundExport: Boolean = fluentWait.until(_ => {
      val awsUtility = AWSUtility()
      awsUtility.isFileInS3(configuration.getString(s"s3.bucket.export.$consignmentType"), s"$consignmentRef.tar.gz")
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

  Then("^the user will be on a page with a heading \"(.*)\"") {
    heading: String =>
      StepsUtility.waitForElementTitle(webDriver, heading, "govuk-heading-m")
  }

  Then("^the user will be on a page with a small heading \"(.*)\"") {
    heading: String =>
      StepsUtility.waitForElementTitle(webDriver, heading, "govuk-heading-s")
  }

  And("^the user should see a banner titled Success") {
    () =>
      StepsUtility.waitForElementTitle(webDriver, "Success", "success-summary__title")
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
      new WebDriverWait(webDriver, 30).until((driver: WebDriver) => {
        webDriver.findElement(By.cssSelector(selector))
      })
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

  And("^the user clicks the (.*) button") {
    button: String =>
      val button = webDriver.findElement(By.cssSelector("[type='submit']"))
      button.click()
  }

  And("^the user clicks the (.*) link") {
    linkName: String =>
      val linkClass = linkName.toLowerCase.replaceAll(" ", "-")
      val link = webDriver.findElement(By.cssSelector(s"a.$linkClass"))
      link.click()
      val client = GraphqlUtility(userCredentials)
      val consignmentRef = client.getConsignmentReference(consignmentId)

      new WebDriverWait(webDriver, 180).until((_: WebDriver) => {
        new File(s"/tmp/$consignmentRef-metadata.csv").exists()
      })
  }

  When("^the user selects yes for all checks except \"I confirm that the records are all Crown Copyright.\"") {
    new WebDriverWait(webDriver, 30).until((driver: WebDriver) => {
      webDriver.findElement(By.id("publicRecord"))
      webDriver.findElement(By.id("crownCopyright"))
    })
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecord"))
    recordsAllPublicRecords.click()
  }

  When("^the user selects yes to all transfer agreement checks") {
    new WebDriverWait(webDriver, 30).until((driver: WebDriver) => {
      webDriver.findElement(By.id("publicRecord"))
      webDriver.findElement(By.id("crownCopyright"))
    })
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecord"))
    val recordsAllCrownCopyright = webDriver.findElement(By.id("crownCopyright"))
    recordsAllPublicRecords.click()
    recordsAllCrownCopyright.click()
  }

  When("^the user selects yes to all transfer agreement continued checks") {
    val recordsDroAppraisal = webDriver.findElement(By.id("droAppraisalSelection"))
    val recordsDroSensitivity = webDriver.findElement(By.id("droSensitivity"))
    new WebDriverWait(webDriver, 30).until((driver: WebDriver) => {
      recordsDroAppraisal
      recordsDroSensitivity
    })
    recordsDroAppraisal.click()
    recordsDroSensitivity.click()
  }

  And("^the user confirms that DRO has signed off on the records") {
    val droAppraisalAndSelection = webDriver.findElement(By.id("droAppraisalSelection"))
    val droSensitivity = webDriver.findElement(By.id("droSensitivity"))
    droAppraisalAndSelection.click()
    droSensitivity.click()
  }

  And("^the user does not confirm DRO sign off for the records") {
    val droAppraisalAndSelection = webDriver.findElement(By.id("droAppraisalSelection"))
    val droSensitivity = webDriver.findElement(By.id("droSensitivity"))
    droAppraisalAndSelection.click()
    droSensitivity.click()
  }

  And("^an existing (.*) consignment for transferring body (.*)") {
    (consignmentType:String, body: String) =>
      val client = GraphqlUtility(userCredentials)
      consignmentId = client.createConsignment(consignmentType, body).get.addConsignment.consignmentid.get
  }

  And("^an selected series (.*)") {
    (body: String) =>
    val client = GraphqlUtility(userCredentials)
    client.updateSeries(consignmentId, body)
  }

  And("^an existing private beta transfer agreement") {
    val client = GraphqlUtility(userCredentials)
    client.createTransferAgreementPrivateBeta(consignmentId)
  }

  And("^an existing compliance transfer agreement") {
    val client = GraphqlUtility(userCredentials)
    client.createTransferAgreementCompliance(consignmentId)
  }

  And("^the file checks are complete") {
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
      client.addFileStatus(fm.fileId, "Antivirus", "Success")
      client.createBackendChecksumMetadata(fm.fileId, checksum)
      client.addFileStatus(fm.fileId, "ChecksumMatch", "Success")
      client.createFfidMetadata(fm.fileId)
      client.addFileStatus(fm.fileId, "FFID", "Success")
    })

  }

  And("^the (checksum|antivirus|FFID) check has (.*)") {
    val client = GraphqlUtility(userCredentials)
    (checkName: String, result: String) => {
      val matchIdInfo = List(MatchIdInfo(checksumValue, Paths.get("."), 0))
      val id = client.addFilesAndMetadata(consignmentId, "E2E TEST UPLOAD FOLDER", matchIdInfo).map(_.fileId).head
      val statusType = checkName.toLowerCase match {
        case "antivirus" => "Antivirus"
        case "ffid" => "FFID"
        case "checksum" => "ChecksumMatch"
      }
      val statusValue = result match {
        case "failed" => "Failed"
        case "succeeded" => "Succeeded"
      }
      client.addFileStatus(id, statusType, statusValue)
    }
  }

  And("^the FFID \"(.*)\" check has failed") {
    (checkName: String) => {
      val client = GraphqlUtility(userCredentials)
      val matchIdInfo = List(MatchIdInfo(checksumValue, Paths.get("."), 0))
      val id: UUID = client.addFilesAndMetadata(consignmentId, "E2E TEST UPLOAD FOLDER", matchIdInfo).map(_.fileId).head
      val statusValue = checkName match {
        case "password protected" =>"PasswordProtected"
        case "zip file" => "Zip"
      }
      client.addFileStatus(id, "FFID", statusValue)
    }
  }

  And("^the user has created additional metadata") {
    val client = GraphqlUtility(userCredentials)
    client.createCustomMetadata(consignmentId)
  }

  Then("^the metadata csv will have the correct columns for (.*) files") {
    numberOfFiles: Int =>
      val client = GraphqlUtility(userCredentials)
      val consignmentRef = client.getConsignmentReference(consignmentId)
      val source = Source.fromFile(s"/tmp/$consignmentRef-metadata.csv")
      val rows = source.getLines().toList
      def filterCsvRows(num: Int): Option[String] = rows.find(_ == s"path$num,FileType-value,1,E2E_tests/original/path$num,RightsCopyright-value,LegalStatus-value,HeldBy-value,2022-09-28T14:31:17,ClosureType-value,2022-09-28T14:31:17,1,FoiExemptionCode-value,2022-09-28T14:31:17,true,TitleAlternate-value,description-value,true,DescriptionAlternate-value,Language-value,2022-09-28T14:31:17,date_range-value,2022-09-28T14:31:17,2022-09-28T14:31:17,file_name_language-value,file_name_translation-value,file_name_translation_language-value,OriginalFilepath-value")

      Assert.assertEquals(rows.size, numberOfFiles + 1)
      val customMetadata = client.getCustomMetadata(consignmentId).filter(_.allowExport).sortBy(_.exportOrdinal.getOrElse(Int.MaxValue))
      val headerRow = rows.head.split(",")
      Assert.assertEquals(headerRow.length, customMetadata.size)
      Assert.assertTrue(filterCsvRows(0).isDefined)
      Assert.assertTrue(filterCsvRows(1).isDefined)
      headerRow.zipWithIndex.map {
        case (title, idx) =>
          val customMetadataName = customMetadata(idx).fullName.getOrElse("")
          Assert.assertEquals(customMetadataName, title)
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

  And("^(\\d+) of the (.*) scans for the (.*) transfer have finished") {
    val client = GraphqlUtility(userCredentials)
    (filesToProcess: Int, metadataType: String, consignmentType: String) => {
      val fileRangeToProcess = createdFiles.slice(0, filesToProcess)
      metadataType match {
        case "antivirus" =>
          fileRangeToProcess.foreach(id => {
            client.createAVMetadata(id)
            client.addFileStatus(id, "Antivirus", "Success")
          })
          filesWithoutAVMetadata = createdFiles.drop(filesToProcess)
        case "FFID" =>
          val puid: String = if (consignmentType == "judgment") { "fmt/412" } else { "x-fmt/111" }
          fileRangeToProcess.foreach(id => {
            client.createFfidMetadata(id, puid)
            client.addFileStatus(id, "FFID", "Success")
          })
          filesWithoutFFIDMetadata = createdFiles.drop(filesToProcess)
        case "checksum" =>
          fileRangeToProcess.foreach(id => {
            client.createBackendChecksumMetadata(id, createdFilesIdToChecksum.get(id))
            client.addFileStatus(id, "ChecksumMatch", "Success")
          })
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
      selectItem(fileName)
    }
  }

  When("^the user selects the file: (.*)") {
    fileName: String => {
      selectItem(fileName)
    }
  }

  And("^the (.*) should be visible") {
    targetIdName: String => {
      val id = targetIdName.replaceAll(" ", "-")
      new WebDriverWait(webDriver, 180).until((driver: WebDriver) => {
        val isVisible = !StepsUtility.elementIsHidden(id, driver)
        isVisible
      })
    }
  }

  Then("^the (.*) should not be visible") {
    (targetIdName: String) => {
      val id = targetIdName.replaceAll(" ", "-")
      new WebDriverWait(webDriver, 10).until((driver: WebDriver) => {
        val isNotVisible = StepsUtility.elementIsHidden(id, webDriver)
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
      new WebDriverWait(webDriver, 180).ignoring(classOf[AssertionError]).until((driver: WebDriver) => {
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
    differentUserId = KeycloakClient.createUser(differentUserCredentials, None, None)
  }

  And("^the user who did not create the consignment is logged in on the (.*) page") {
    page: String =>
      loadPage(page)
      StepsUtility.userLogin(webDriver, differentUserCredentials)
  }

  Then("^the user who did not create the consignment will see the error message \"(.*)\"") {
    errorMessage: String =>
      val selector = ".govuk-heading-l"
       new WebDriverWait(webDriver, 10).ignoring(classOf[AssertionError]).until((driver: WebDriver) => {
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

  And("^the user confirms that they are transferring legal custody of the records to TNA") {
    val transferLegalCustody = webDriver.findElement(By.id("transferLegalCustody"))
    transferLegalCustody.click()
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
    confirmTransferKeys.forEach(key => {
     val keyText = key.getText
      Assert.assertTrue("Confirm transfer list key empty", !keyText.isEmpty)
      Assert.assertTrue("Confirm transfer list key is incorrect",expectedKeys.contains(keyText))
    })

    Assert.assertTrue(confirmTransferValues.size == 4)
    confirmTransferValues.foreach(value => {
      Assert.assertTrue("Confirm transfer list value empty", !value.getText.isEmpty)
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

  Then("^the download metadata page elements are loaded") {
    val href = webDriver.findElement(By.cssSelector("a.download-metadata")).getAttribute("href")
    val buttonText = webDriver.findElement(By.cssSelector("a.govuk-button")).getText
    val svgClass = webDriver.findElement(By.cssSelector("svg")).getAttribute("class")

    Assert.assertEquals(s"$baseUrl/consignment/$consignmentId/additional-metadata/download-metadata/csv", href)
    Assert.assertEquals("Continue", buttonText)
    Assert.assertEquals("thumbnail-icon", svgClass)
  }
}
