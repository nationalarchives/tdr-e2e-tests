package helpers.steps

import java.nio.file.{Files, Path}
import java.security.MessageDigest
import helpers.keycloak.UserCredentials
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.{By, StaleElementReferenceException, WebDriver, WebElement}
import scala.jdk.CollectionConverters._
import java.time.Duration

object StepsUtility {
  def waitForElementTitle(webDriver: WebDriver, title: String, elementClassName: String): Any = {
    new WebDriverWait(webDriver, Duration.ofSeconds(180)).withMessage{
      s"""\nCould not find title "$title" in any elements belonging to class "$elementClassName" on the page:
         |${webDriver.getCurrentUrl}
         |
         |Below is the page source:
         |
         |${webDriver.getPageSource}""".stripMargin}
      .ignoring(classOf[StaleElementReferenceException])
      /*Ignore stale references exceptions.
      These seem to happen when Selenium selects an element which then disappears when the user is redirected to the next page,
      such as from the upload page to the file checks page. In this case, we only want to check the element on the second page,
      so it doesn't matter if the same element on the first page has disappeared.*/
      .until((driver: WebDriver) => {
        val panels: List[WebElement] = webDriver.findElements(By.className(elementClassName)).asScala.toList
        println("Last page: " + webDriver.getPageSource)
        panels.exists(_.getText == title)
      })
  }

  def userLogin(webDriver: WebDriver, userCredentials: UserCredentials): Unit = {
    enterUserCredentials(webDriver, userCredentials)
    val clickableElement = webDriver.findElement(By.cssSelector("[name='login']"))
    clickableElement.click()
  }

  def enterUserCredentials(webDriver: WebDriver, userCredentials: UserCredentials): Unit = {
    new WebDriverWait(webDriver, Duration.ofSeconds(30)).withMessage{
      s"""Could not find username or password field on this page ${webDriver.getCurrentUrl}
          |Below is the page source:
          |
          |${webDriver.getPageSource}""".stripMargin
    }.until(
      (driver: WebDriver) => {
        val userNameElement = webDriver.findElement(By.cssSelector("[name='username']"))
        val passwordElement = webDriver.findElement(By.cssSelector("[name='password']"))

        userNameElement.sendKeys(userCredentials.email)
        passwordElement.sendKeys(userCredentials.password)
      }
    )
  }

  def elementIsHidden(id: String, webDriver: WebDriver): Boolean = {
    val element = webDriver.findElement(By.cssSelector(s"#$id"))
    val hideCssClasses = List("hide", "govuk-visually-hidden")
    hideCssClasses.exists(element.getAttribute("class").contains) || Option(element.getAttribute("hidden")).isDefined
  }

  def elementIsSelected(id: String, webDriver: WebDriver): Boolean = {
    val element = webDriver.findElement(By.cssSelector(s"#$id"))
    element.isSelected
  }

  def elementHasClassDisabled(id: String, webDriver: WebDriver): Boolean = {
    val element = webDriver.findElement(By.cssSelector(s"#$id"))
    element.getAttribute("class").contains("govuk-button--disabled")
  }

  def calculateTestFileChecksum(filePath: Path): String = {
    val messageDigester: MessageDigest = MessageDigest.getInstance("SHA-256")

    val arr = Files readAllBytes filePath
    val checksum = messageDigester digest arr
    checksum.map("%02x" format _).mkString
  }
}
