package steps

import com.github.tototoshi.csv.{CSVReader, CSVWriter}
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
import io.cucumber.scala.{EN, ScalaDsl, Scenario}
import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.junit.Assert
import org.openqa.selenium._
import org.openqa.selenium.support.ui.{ExpectedConditions, FluentWait, Select, WebDriverWait}

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.net.URI
import java.nio.file.Paths
import java.time.Duration
import java.util
import java.util.UUID
import scala.io.Source
import scala.jdk.CollectionConverters._
import scala.util.Try

class Steps extends ScalaDsl with EN {
  var webDriver: WebDriver = _
  var userId: String = ""
  var userType: String = ""
  var differentUserId: String = ""
  var tnaUserId: String = ""
  var consignmentId: UUID = _
  var consignmentRef: String = _
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
  val tnaEmail: String = s"${RandomUtility.randomString()}@testSomething.com"
  val invalidEmail: String = "dgfhfdgjhgfj"
  val password: String = RandomUtility.randomString(10)
  val differentPassword: String = RandomUtility.randomString(10)
  val tnaPassword: String = RandomUtility.randomString(10)
  val invalidPassword: String = "fdghfdgh"
//  var userCredentials: UserCredentials = _
//  var differentUserCredentials: UserCredentials = _
//  var tnaUserCredentials: UserCredentials = _

  var userCredentials: UserCredentials = UserCredentials(email, password)
  var differentUserCredentials: UserCredentials = UserCredentials(differentEmail, differentPassword)
  var tnaUserCredentials: UserCredentials = UserCredentials(tnaEmail, tnaPassword)

  val invalidUserCredentials: UserCredentials = UserCredentials(invalidEmail, invalidPassword)
  val checksumValue = "checksum"

  def waitTime(n: Long): Duration = { Duration.ofSeconds(n)}

  Before { scenario : Scenario =>
    setUserCredentialsForScenario(scenario)
    webDriver = initDriver
  }

  private def setUserCredentialsForScenario(scenario: Scenario): Unit = {
    val featureName = scenario.getUri.toURL.getFile.split('/').last
    val scenarioName = scenario.getName.take(250).replaceAll("[^a-zA-Z0-9]+"," ")

    userCredentials = userCredentials.copy(lastName = featureName, firstName = scenarioName)
    differentUserCredentials = differentUserCredentials.copy(lastName = featureName, firstName = scenarioName)
    tnaUserCredentials = tnaUserCredentials.copy(lastName = featureName, firstName = scenarioName)

//    userCredentials = UserCredentials(email, password, lastName = featureName, firstName = scenarioName)
//    differentUserCredentials = UserCredentials(differentEmail, differentPassword, lastName = featureName, firstName = scenarioName)
//    tnaUserCredentials = UserCredentials(tnaEmail, tnaPassword, lastName = featureName, firstName = scenarioName)
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

  private def logout(): Unit = {
    Try {
      val signOutElement = webDriver.findElement(By.cssSelector("header[role='banner'] li:nth-child(2) a:nth-child(1)"))
      signOutElement.click()
    }.recover {
      case _: NoSuchElementException =>
        webDriver.findElement(By.cssSelector("a[class='govuk-header__link']")).click()
    }
  }

  private def loadPage(page: String): Unit = {
    val hyphenatedPageName = page.toLowerCase.replaceAll(" ", "-")
    val isJudgment = userType == "judgment"
    val path = if (isJudgment) "judgment" else "consignment"
    val pageWithConsignment = hyphenatedPageName match {
      case "homepage" | "view-transfers" | "some-page" => s"$baseUrl/$hyphenatedPageName"
      case "faq" | "help" => if (isJudgment) s"$baseUrl/$path/$hyphenatedPageName" else s"$baseUrl/$hyphenatedPageName"
      case "download-metadata" => s"$baseUrl/$path/$consignmentId/additional-metadata/$hyphenatedPageName"
      case "additional-metadata-entry" => s"$baseUrl/$path/$consignmentId/additional-metadata/entry-method"
      case "draft-metadata-upload" => s"$baseUrl/$path/$consignmentId/draft-metadata/upload"
      case "review-progress" => s"$baseUrl/$path/$consignmentId/metadata-review/review-progress"
      case "metadata-review" => s"$baseUrl/admin/metadata-review/$consignmentId"
      case _ => s"$baseUrl/$path/$consignmentId/$hyphenatedPageName"
    }
    webDriver.get(pageWithConsignment)
  }

  private def loadPageByMetadataType(page: String, metadataType: String): Unit = {
    val hyphenatedPageName = page.toLowerCase.replaceAll(" ", "-")
    val pageWithConsignment = hyphenatedPageName match {
      case "files-selection" => s"$baseUrl/consignment/$consignmentId/additional-metadata/files/$metadataType"
      case "confirm-closure-status" => s"$baseUrl/consignment/$consignmentId/additional-metadata/status/closure?fileIds=${createdFiles.head}"
      case "add-metadata" => s"$baseUrl/consignment/$consignmentId/additional-metadata/add/$metadataType?fileIds=${createdFiles.head}"
      case "view-metadata" => s"$baseUrl/consignment/$consignmentId/additional-metadata/selected-summary/$metadataType?fileIds=${createdFiles.head}"
      case "entry-method" => s"$baseUrl/consignment/$consignmentId/additional-metadata/entry-method"
      case _ => s"$baseUrl/consignment/$consignmentId/$hyphenatedPageName"
    }
    webDriver.get(pageWithConsignment)
  }

  private def userCleanUp(): Unit = {
    this.userType = ""
    KeycloakClient.deleteUser(userId)

    //Not all scenarios create the different and/or tna user
    if (differentUserId.nonEmpty) {
      KeycloakClient.deleteUser(differentUserId)
    }
    if (tnaUserId.nonEmpty) {
      KeycloakClient.deleteUser(tnaUserId)
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
    val filePath = s"${System.getProperty("user.dir")}/src/test/resources/testfiles/$fileName"

    if (fileName == "draft-metadata.csv" || fileName == "invalid-draft-metadata.csv") {
      val reader = CSVReader.open(filePath)
      val data = reader.all()
      val fileIds = "UUID" :: createdFiles.map(_.toString)

      val updatedValues = data.zip(fileIds).map(p => p._1 :+ p._2)
      val utf8BOM = Array(0xEF.toByte, 0xBB.toByte, 0xBF.toByte)
      val bos = new BufferedOutputStream(new FileOutputStream(s"/tmp/$fileName"))
      bos.write(utf8BOM)
      bos.close()
      val writer = CSVWriter.open(s"/tmp/$fileName", append=true)
      writer.writeAll(updatedValues)
      webDriver.findElement(By.cssSelector("#file-selection")).sendKeys(s"/tmp/$fileName")
    } else {
      //Wait for the cookies endpoint to respond. There is no visible change to the page when this happens so we just sleep.
      Thread.sleep(15 * 1000)
      webDriver.findElement(By.cssSelector("#file-selection")).sendKeys(filePath)
      webDriver.asInstanceOf[JavascriptExecutor].executeScript(s"Object.defineProperty(document.querySelector('#file-selection').files[0], 'webkitRelativePath', {value: 'testfiles/$fileName'})")
    }
  }

  private def getDownloadedCsv(name: String, downloadPath: String = "/tmp/"): Array[File] = {
    val expectedFileExtension = ".xlsx"
    val dir = new File(downloadPath)
    val files = dir.listFiles()
    files.filter(f => f.getName.startsWith(name) && f.getName.endsWith(expectedFileExtension))
  }

  private def getSummaryMetadata: Map[String, String] = {
    val fields = webDriver.findElements(By.cssSelector(s".govuk-summary-list__key")).asScala.toList.map(_.getText)
    val values = webDriver.findElements(By.cssSelector(s".govuk-summary-list__value")).asScala.toList.map(_.getText.trim)
    (fields zip values).toMap
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
      val credential: UserCredentials = userType match {
        case "transfer adviser" | "metadata viewer" =>
          tnaUserId = KeycloakClient.createUser(tnaUserCredentials, None, Some(userType.replace(" ", "_")))
          tnaUserCredentials
        case _ =>
          userId = KeycloakClient.createUser(userCredentials, Some("Mock 1 Department"), Some(userType))
          userCredentials
      }
      login(credential)
      this.userType = userType
  }

  Given("^an existing (.*) user logs in") {
    userType: String =>
      login(userCredentials)
  }

  Then("^the (.*) user logs out") {
    _: String =>
      lazy val client = GraphqlUtility(userCredentials)
      consignmentRef = client.getConsignmentReference(consignmentId)
      logout()
  }

  And("^the user is logged in on the (.*) page") {
    page: String =>
      loadPage(page)
      StepsUtility.userLogin(webDriver, userCredentials)
      val pageNameInUrl = page.toLowerCase.replaceAll(" ", "-")

      new WebDriverWait(webDriver, waitTime(30)).withMessage {
        s"""Could not find page name "$pageNameInUrl" in this URL ${webDriver.getCurrentUrl}
            |Below is the page source:
            |
            |${webDriver.getPageSource}""".stripMargin
      }.until((driver: WebDriver) => {
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

  When("^the logged in user navigates to the (.*) page for (.*) metadata") {
    (page: String, metadataType: String) =>
      loadPageByMetadataType(page, metadataType)
  }

  And("^the (.*) page is loaded") {
    page: String =>
      loadPage(page)
  }

  And("^the user clicks on the (.*)(?: button|link)$") {
    button: String =>
      if (button == "Download report") {
        webDriver.findElement(By.linkText(button.trim)).click()
        new WebDriverWait(webDriver, waitTime(10)).withMessage {
          "There were no files in the directory containing the name ErrorReport"
        }.until((driver: WebDriver) => {
          val filteredFiles = getDownloadedCsv("ErrorReport")
          filteredFiles.nonEmpty
        })
      } else {
        new WebDriverWait(webDriver, waitTime(30)).withMessage {
          s"""Could not find button "$button" on this page ${webDriver.getCurrentUrl}
             |Below is the page source:
             |
             |${webDriver.getPageSource}""".stripMargin
        }.until(
          (driver: WebDriver) => webDriver.findElement(By.linkText(button.trim)).click()
        )
      }
  }

  Then("^the (.*) button is not displayed on the page") {
    button: String =>
      webDriver.findElements(By.linkText(button)).isEmpty
  }

  Then("^the logged out user should be on the login page") {
    new WebDriverWait(webDriver, waitTime(30)).withMessage {
      s"""URL ${webDriver.getCurrentUrl} did not start with $authUrl""".stripMargin
    }.until((driver: WebDriver) => {
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
      new WebDriverWait(webDriver, waitTime(30)).withMessage {
        s"""URL ${webDriver.getCurrentUrl} did not start or end with "$page"""".stripMargin
      }.until((driver: WebDriver) => {
        val currentUrl: String = webDriver.getCurrentUrl
        currentUrl.startsWith(s"$baseUrl/$page") || currentUrl.endsWith(page)
      })
      val currentUrl: String = webDriver.getCurrentUrl

      Assert.assertTrue(doesNotMatchExpected(currentUrl, page), currentUrl.startsWith(s"$baseUrl/$page") || currentUrl.endsWith(page))
  }

  Then("^the user should be on a page with (.*) and a consignmentId in the URL") {
    page: String =>
      val nonConsignmentIds = Set("judgment", "consignment", "nationalarchives.gov.uk")
      new WebDriverWait(webDriver, waitTime(30)).withMessage {
        s"""Could not find consignmentId in url ${webDriver.getCurrentUrl}""".stripMargin
      }.until((driver: WebDriver) => {
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
    lazy val client = GraphqlUtility(userCredentials)
    val consignmentRef = client.getConsignmentExport(consignmentId).get.getConsignment.get.consignmentReference

    val fluentWait = new FluentWait[WebDriver](webDriver)
      .withTimeout(waitTime(600))
      .pollingEvery(waitTime(10))

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

  Then("^the user will be on a page with the fieldset heading \"(.*)\"") {
    page: String =>
      StepsUtility.waitForElementTitle(webDriver, page, "govuk-fieldset__heading")
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

  Then("^the user will be on a page with the large heading \"(.*)\"") {
    page: String =>
      StepsUtility.waitForElementTitle(webDriver, page, "govuk-heading-xl")
  }

  Then("^the user will be on the \"(.*)\" \"(.*)\" page") {
    ( caption: String, title: String) =>
      StepsUtility.waitForElementTitle(webDriver, caption, "govuk-caption-l")
      StepsUtility.waitForElementTitle(webDriver, title, "govuk-heading-l")
  }

  Then("^the user will be on a page with the label \"(.*)\"") {
    page: String =>
      StepsUtility.waitForElementTitle(webDriver, page, "govuk-label")
  }

  Then("^the user will see the (.*) alert") {
    text: String =>
      val expectedAlert = text match {
        case "Approve" => "You can now complete your transfer"
        case "Reject" => "We found issues in your metadata"
        case _ => "Your review is in progress"
      }
      StepsUtility.waitForElementTitle(webDriver, expectedAlert, "da-alert__heading")
  }

  And("^the user will see a row with a consignment reference that correlates with their consignmentId") {
    () =>
      lazy val client = GraphqlUtility(userCredentials)
      val consignmentRef = client.getConsignmentReference(consignmentId)
      StepsUtility.waitForElementTitle(webDriver, s"$consignmentRef", "govuk-table__header")
  }

  And("^the user should see a banner titled Success") {
    () =>
      StepsUtility.waitForElementTitle(webDriver, "Success", "govuk-notification-banner__title")
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
      new WebDriverWait(webDriver, waitTime(30)).withMessage {
        s"""Could not find error message $formErrorMessage on page ${webDriver.getCurrentUrl}
           |Below is the page source:
           |
           |${webDriver.getPageSource}""".stripMargin
      }.until((driver: WebDriver) => {
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

  And("^the (.*) user (.*) the metadata") {
    (userType: String, status: String) =>
      val statusDropdown = new Select(webDriver.findElement(By.name("status")))
      statusDropdown.selectByVisibleText(status)
  }

  And("^the user selects the option (.*)") {
    option: String =>
      val selectedOption = option match {
        case "Select records and add metadata" => "#metadata-route-manual"
        case "Add metadata to a CSV and upload" => "#metadata-route-csv"
        case "I don't have any metadata" => "#metadata-route-none"
      }
      val radioButton = webDriver.findElement(By.cssSelector(selectedOption))
      radioButton.click()
  }

  And("^the user clicks the (.*) button") {
    button: String =>
      Try {
        val button1 = webDriver.findElement(By.cssSelector("form [type='submit']"))
        button1.click()
      }.recover {
        case _: NoSuchElementException =>
          webDriver.findElement(By.cssSelector("[data-module='govuk-button']")).click()
      }
  }

  And("^the user clicks the (.*) link") {
    linkName: String =>
      if (linkName == "download metadata") {
        val linkClass = linkName.toLowerCase.replaceAll(" ", "-")
        val link = webDriver.findElement(By.cssSelector(s"a.$linkClass"))
        link.click()
        lazy val client = GraphqlUtility(userCredentials)
        val consignmentRef = client.getConsignmentReference(consignmentId)
        val filteredFiles = getDownloadedCsv(consignmentRef)

        if (filteredFiles.nonEmpty) {
          Assert.assertTrue(filteredFiles.last.exists())
        } else {
          Assert.fail(s"There were no files in the directory containing the name $consignmentRef")
        }
      } else if(linkName == "Delete metadata") {
        webDriver.findElement(By.id("deleteMetadata")).click()
      } else {
        val link = webDriver.findElement(By.cssSelector(s".govuk-button.govuk-button--secondary"))
        link.click()
      }
  }

  When("^the user selects yes for all checks except \"I confirm that the records are all Crown Copyright.\"") {
    new WebDriverWait(webDriver, waitTime(30)).withMessage {
      s"""Could not find id publicRecord or crownCopyright on page ${webDriver.getCurrentUrl}
         |Below is the page source:
         |
         |${webDriver.getPageSource}""".stripMargin
    }.until((driver: WebDriver) => {
      webDriver.findElement(By.id("publicRecord"))
      webDriver.findElement(By.id("crownCopyright"))
    })
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecord"))
    recordsAllPublicRecords.click()
  }

  When("^the user selects yes to all transfer agreement part 1 checks") {
    new WebDriverWait(webDriver, waitTime(30)).withMessage {
      s"""Could not find id publicRecord or crownCopyright on page ${webDriver.getCurrentUrl}
         |Below is the page source:
         |
         |${webDriver.getPageSource}""".stripMargin
    }.until((driver: WebDriver) => {
      webDriver.findElement(By.id("publicRecord"))
      webDriver.findElement(By.id("crownCopyright"))
    })
    val recordsAllPublicRecords = webDriver.findElement(By.id("publicRecord"))
    val recordsAllCrownCopyright = webDriver.findElement(By.id("crownCopyright"))
    recordsAllPublicRecords.click()
    recordsAllCrownCopyright.click()
  }

  When("^the user selects yes to all transfer agreement part 2 checks") {

    new WebDriverWait(webDriver, waitTime(30)).withMessage {
      s"""Could not find id droAppraisalSelection, droSensitivity or openRecords on page ${webDriver.getCurrentUrl}
         |Below is the page source:
         |
         |${webDriver.getPageSource}""".stripMargin
    }.until((driver: WebDriver) => {
      webDriver.findElement(By.id("droAppraisalSelection"))
      webDriver.findElement(By.id("droSensitivity"))
    })
    val recordsDroAppraisal = webDriver.findElement(By.id("droAppraisalSelection"))
    val recordsDroSensitivity = webDriver.findElement(By.id("droSensitivity"))

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
      lazy val client = GraphqlUtility(userCredentials)
      consignmentId = client.createConsignment(consignmentType, body).get.addConsignment.consignmentid.get
  }

  And("^an existing selected series (.*)") {
    (body: String) =>
    lazy val client = GraphqlUtility(userCredentials)
    client.addConsignmentStatus(consignmentId, "Series", "Completed")
    client.updateSeries(consignmentId, body)
  }

  And("^an existing transfer agreement part 1") {
    lazy val client = GraphqlUtility(userCredentials)
    client.createTransferAgreementPrivateBeta(consignmentId)
  }

  And("^an existing transfer agreement part 2") {
    lazy val client = GraphqlUtility(userCredentials)
    client.createTransferAgreementCompliance(consignmentId)
  }

  And("^the file checks are complete") {
    lazy val client = GraphqlUtility(userCredentials)
    client.startUpload(consignmentId)
    client.updateConsignmentStatus(consignmentId, "Upload", "Completed")
    client.updateConsignmentStatus(consignmentId, "ClientChecks", "Completed")
    val files = List("testfile1.txt", "testfile2.txt")
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
      client.createBackendChecksumMetadata(consignmentId, List(fm.fileId), checksum)
      client.addFileStatus(fm.fileId, "ChecksumMatch", "Success")
      client.createFfidMetadata(fm.fileId)
      client.addFileStatus(fm.fileId, "FFID", "Success")
    })
  }

  And("^the (checksum|antivirus|FFID) check has (.*)") {
    lazy val client = GraphqlUtility(userCredentials)
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
      lazy val client = GraphqlUtility(userCredentials)
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
    lazy val client = GraphqlUtility(userCredentials)
    client.createCustomMetadata(consignmentId)
  }

  Then("^the metadata csv will have the correct columns for (.*) files") {
    numberOfFiles: Int =>
      lazy val client = GraphqlUtility(userCredentials)
      val consignmentRef = client.getConsignmentReference(consignmentId)
      val metadataCsv = getDownloadedCsv(consignmentRef).last
      val source = Source.fromFile(metadataCsv.getAbsolutePath)
      val rows = source.getLines().toList
      def filterCsvRows(num: Int): Option[String] = rows.find(_ == s"path$num,E2E_tests/original/path$num,2022-09-28,ClosureType-value,2022-09-28,1,FoiExemptionCode-value,2022-09-28,Yes,TitleAlternate-value,description-value,Yes,DescriptionAlternate-value,Language-value,2022-09-28,file_name_translation-value,former_reference_department-value,UUID-value")
      case class DisplayProperty(active: Boolean, name: String, propertyName: String)
      Assert.assertEquals(rows.size, numberOfFiles + 1)
      val systemValues = List("UUID", "Filename", "ClientSideOriginalFilepath", "ClientSideFileLastModifiedDate")
      val displayProperties = client.getDisplayProperties(consignmentId).map(_.displayProperties.map(dp => {
        val active = dp.attributes.find(_.attribute == "Active").flatMap(_.value).contains("true")
        val name = dp.attributes.find(_.attribute == "Name").flatMap(_.value).getOrElse("")
        DisplayProperty(active, name, dp.propertyName)
      })).getOrElse(Nil).filter(dp => dp.active || systemValues.contains(dp.propertyName)).groupBy(_.propertyName)

      val customMetadata = client.getCustomMetadata(consignmentId)
        .filter(cm => cm.allowExport && displayProperties.keySet.contains(cm.name))
        .sortBy(_.exportOrdinal.getOrElse(Int.MaxValue))

      val headerRow = rows.head.split(",")
      Assert.assertEquals(headerRow.length, customMetadata.size)
      Assert.assertTrue(filterCsvRows(0).isDefined)
      Assert.assertTrue(filterCsvRows(1).isDefined)
      headerRow.zipWithIndex.map {
        case (title, idx) =>
          val customMetadataName = displayProperties(customMetadata(idx).name).headOption.map(_.name).getOrElse("")
          Assert.assertEquals(customMetadataName, title)
      }
  }

  Then("^the downloaded metadata csv should be same as (.*)") {
    fileName: String =>
      lazy val client = GraphqlUtility(userCredentials)
      val consignmentRef = client.getConsignmentReference(consignmentId)
      val metadataCsv = getDownloadedCsv(consignmentRef).last
      val source = Source.fromFile(metadataCsv.getAbsolutePath)
      val actualRows = source.getLines().map(_.split(",").drop(3).mkString(",")).toList.sorted

      val draftMetadataSource = Source.fromFile(s"/tmp/$fileName")
      val expectedRows = draftMetadataSource.getLines().map(_.split(",").drop(3).mkString(",")).toList.sorted
      Assert.assertTrue(expectedRows.containsSlice(actualRows))
  }

  Then("^the error report should be same as (.*)") {
    fileName: String =>
      val metadataCsv = getDownloadedCsv("ErrorReport").last
      val wb = new ReadableWorkbook(metadataCsv)
      val sheet = wb.getFirstSheet
      val actualRows = sheet.read().asScala.map(_.asScala.map(_.getText).mkString(",")).toList
      val expectedErrorFilePath = s"${System.getProperty("user.dir")}/src/test/resources/testfiles/$fileName"
      val reader = CSVReader.open(new File(expectedErrorFilePath))
      val expectedRows = reader.all().map(_.mkString(","))
      reader.close()
      Assert.assertEquals(expectedRows, actualRows)
  }

  And("^an existing upload of (\\d+) files") {
    lazy val client = GraphqlUtility(userCredentials)
    numberOfFiles: Int => {
      val files = List("testfile1.txt", "testfile2.txt")

      val matchIdInfo: List[MatchIdInfo] = List.tabulate(numberOfFiles)(n => n).map(idx => {
        val path = Paths.get(s"${System.getProperty("user.dir")}/src/test/resources/testfiles/${files(idx % 2)}")
        val checksumValue = calculateTestFileChecksum(path)
        MatchIdInfo(checksumValue, path, idx)
      })
      val addFilesAndMetadataResult = client.addFilesAndMetadata(consignmentId,  "E2E TEST UPLOAD FOLDER", matchIdInfo)
      createdFiles = addFilesAndMetadataResult.sortBy(_.matchId).map(_.fileId)

      val awsUtility = AWSUtility()

      client.updateConsignmentStatus(consignmentId, "Upload", "Completed")
      client.updateConsignmentStatus(consignmentId, "ClientChecks", "Completed")
      createdFilesIdToChecksum = addFilesAndMetadataResult.map(res => {
        val info: MatchIdInfo = matchIdInfo.find(_.matchId == res.matchId).get
        awsUtility.uploadFileToS3(configuration.getString("s3.bucket.upload"), s"$consignmentId/${res.fileId}", info.path)
        res.fileId -> info.checksum
      }).toMap
    }
  }

  And("^(\\d+) of the (.*) scans for the (.*) transfer have finished") {
    lazy val client = GraphqlUtility(userCredentials)
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
            client.createBackendChecksumMetadata(consignmentId, List(id), createdFilesIdToChecksum.get(id))
            client.addFileStatus(id, "ChecksumMatch", "Success")
          })
          filesWithoutChecksumMetadata = createdFiles.drop(filesToProcess)
      }
    }
  }

  And("^the user waits for the checks to complete") {
    lazy val client = GraphqlUtility(userCredentials)
    filesWithoutChecksumMetadata.foreach {
      id =>
        client.createBackendChecksumMetadata(consignmentId, List(id), createdFilesIdToChecksum.get(id))
        client.addFileStatus(id, "ChecksumMatch", "Success")
    }

    filesWithoutAVMetadata.foreach {
      id =>
        client.createAVMetadata(id)
        client.addFileStatus(id, "Antivirus", "Success")
    }

    filesWithoutFFIDMetadata.foreach {
      id =>
        client.createFfidMetadata(id)
        client.addFileStatus(id, "FFID", "Success")
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

  When("^the user selects (.+) checkbox") {
    targetIdName: String => {
      val element = new WebDriverWait(webDriver, waitTime(5)).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(s"[for=$targetIdName]")));
      element.click()
    }
  }

  And("^the (.*) should be visible") {
    targetIdName: String => {
      val id = targetIdName.replaceAll(" ", "-")
      new WebDriverWait(webDriver, waitTime(180)).withMessage {
        s"""Could not find id $id on page ${webDriver.getCurrentUrl}
           |Below is the page source:
           |
           |${webDriver.getPageSource}""".stripMargin
      }.until((driver: WebDriver) => {
        val isVisible = !StepsUtility.elementIsHidden(id, driver)
        isVisible
      })
    }
  }

  Then("^the (.*) should not be visible") {
    (targetIdName: String) => {
      val id = targetIdName.replaceAll(" ", "-")
      new WebDriverWait(webDriver, waitTime(10)).withMessage {
        s"""Could not find id $id on page ${webDriver.getCurrentUrl}
           |Below is the page source:
           |
           |${webDriver.getPageSource}""".stripMargin
      }.until((driver: WebDriver) => {
        val isNotVisible = StepsUtility.elementIsHidden(id, webDriver)
        isNotVisible
      })
    }
  }

  Then("^the (.+) checkbox should be unchecked") {
    (id: String) => {
      Assert.assertFalse(s"$id checkbox should be unchecked", StepsUtility.elementIsSelected(id, webDriver))
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
      new WebDriverWait(webDriver, waitTime(180)).ignoring(classOf[AssertionError]).withMessage {
        s"""Could not find id $id on page ${webDriver.getCurrentUrl}
           |Below is the page source:
           |
           |${webDriver.getPageSource}""".stripMargin
      }.until((driver: WebDriver) => {
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

  And("^the user who did not create the consignment is logged in on the (.*) page for (.*) metadata") {
    (page: String, metadataType: String) =>
      loadPageByMetadataType(page, metadataType)
      StepsUtility.userLogin(webDriver, differentUserCredentials)
  }

  Then("^the user will be on a page with the error message \"(.*)\"") {
    errorMessage: String =>
      val selector = ".govuk-heading-l"
       new WebDriverWait(webDriver, waitTime(10)).ignoring(classOf[AssertionError]).withMessage {
         s"""Could not find class $selector on page ${webDriver.getCurrentUrl}
            |Below is the page source:
            |
            |${webDriver.getPageSource}""".stripMargin
       }.until((driver: WebDriver) => {
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
    confirmTransferKeys.foreach(key => {
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
    val buttonText = webDriver.findElements(By.cssSelector("a.govuk-button")).asScala.last.getText
    val svg = webDriver.findElement(By.cssSelector("svg"))

    Assert.assertEquals(s"$baseUrl/consignment/$consignmentId/additional-metadata/download-metadata/csv", href)
    Assert.assertEquals("Continue", buttonText)
    Assert.assertNotNull(svg)
  }

  And("^the user enters (.*) for the (.*) field") {
    (value: String, field: String) => {
      enterMetadata(value, field)
    }
  }

  private def enterMetadata(value: String, field: String): Unit = field match {
    case "description" => webDriver.findElement(By.cssSelector(s"#inputtextarea-$field")).sendKeys(value)
    case "date of the record" | "FOI decision asserted" | "closure start date" =>
      val input = if (field == "date of the record") {
        "date-input-end_date"
      } else if (field == "FOI decision asserted") {
        "date-input-FoiExemptionAsserted"
      } else if (field == "closure start date") {
        "date-input-ClosureStartDate"
      }
      val List(day, month, year) = value.split("/").toList
      webDriver.findElement(By.cssSelector(s"#$input-day")).sendKeys(day)
      webDriver.findElement(By.cssSelector(s"#$input-month")).sendKeys(month)
      webDriver.findElement(By.cssSelector(s"#$input-year")).sendKeys(year)
    case "closure period" => webDriver.findElement(By.id("Years")).sendKeys(value)
    case "translated title" =>
      webDriver.findElement(By.name("inputtext-file_name_translation-")).sendKeys(value)
    case "former reference" => webDriver.findElement(By.name("inputtext-former_reference_department-")).sendKeys(value)
  }

  And("^the user confirms the closure status of the selected file") {
    val closureStatus = webDriver.findElement(By.id("closureStatus"))
    closureStatus.click()
  }

  And("^the user (selects|de-selects) (.*) for the (.*) field") {
    (_: String, value: String, _: String) => {
      val selected = webDriver.findElement(By.cssSelector(s"[value=$value]"))
      selected.click()
    }
  }

  And("^an existing completed (.*) form") {
    (metadataType: String) =>
      lazy val client = GraphqlUtility(userCredentials)
      client.saveMetadata(consignmentId, createdFiles, metadataType)
  }

  And("^an existing metadata review is in progress") {
    lazy val client = GraphqlUtility(userCredentials)
    val updateStatus = client.addConsignmentStatus(consignmentId, "MetadataReview", "InProgress")
    Assert.assertTrue(updateStatus.nonEmpty)
  }

  And("^existing metadata should contain (.*) values") {
    (numberOfMetadata: Int) =>
      val fieldValues = getSummaryMetadata
      Assert.assertTrue(fieldValues.size == numberOfMetadata)
  }

  And("^existing metadata should contain the metadata (.*) with value (.*)") {
    (metadata: String, value: String) =>
      val fieldValues = getSummaryMetadata
      Assert.assertTrue(fieldValues.contains(metadata))
      Assert.assertTrue(fieldValues.values.exists(_ == value))
  }

  And("^the draft metadata upload status should be \"(.*)\"") {
    (status: String) =>
      val value = webDriver.findElement(By.cssSelector(".govuk-summary-list__value .govuk-tag")).getText
      Assert.assertEquals(status, value)
  }

  And("^the (.*) user clicks view request for consignment") {
    (userType: String) =>
      val rows: List[WebElement] = webDriver.findElements(By.cssSelector("tr.govuk-table__row")).asScala.toList
      var found = false  // Boolean flag to control the loop

      // Iterate through the rows to find the one with the header consignmentRef
      for (row <- rows if !found) {
        val header: WebElement = row.findElement(By.cssSelector("th.govuk-table__header"))

        if (header.getText.contains(consignmentRef)) {
          // Once found, click the "View request" link within that row
          val viewRequestLink: WebElement = row.findElement(By.cssSelector("a.govuk-link"))
          viewRequestLink.click()
          found = true
        }
      }
  }

  And("^the label \"(.*)\" should not be visible for the (.*) user") {
    (label: String, _: String) =>
      val panels: List[WebElement] = webDriver.findElements(By.className("govuk-label")).asScala.toList
      Assert.assertFalse(panels.exists(_.getText == label))
  }
}
