package runners

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import io.circe.generic.auto._
import io.circe.parser.decode
import io.cucumber.core.cli.Main

import java.io.{ByteArrayInputStream, InputStream, OutputStream}
import java.nio.file.{Files, Paths}
import scala.io.Source

class Lambda extends RequestStreamHandler {
  case class Filter(feature: String, nodeUrl: String, browser: String)

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val inputString = Source.fromInputStream(input).mkString
    decode[Filter](inputString) match {
      case Left(_) => throw new Exception("No feature file name provided")
      case Right(filter) =>
        System.setProperty("selenium.node.url", filter.nodeUrl)
        System.setProperty("browser", filter.browser)
        val exitStatus = Main.run(s"classpath:features/${filter.feature}")
        if(exitStatus != 0x0) {
          throw new Exception("Tests have failed")
        }
    }
  }
}
//object Lambda extends App {
//  val i = new ByteArrayInputStream("""{"feature": "Series.feature", "browser" : "Firefox"}""".getBytes())
//  new Lambda().handleRequest(i, null, null)
//}
