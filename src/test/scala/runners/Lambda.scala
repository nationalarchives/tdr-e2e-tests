package runners

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import io.circe.parser.decode
import io.cucumber.core.cli.Main
import io.cucumber.core.runtime.ExitStatus

import java.io.{InputStream, OutputStream}
import scala.io.Source

class Lambda extends RequestStreamHandler {
  case class Filter(feature: String)

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val inputString = Source.fromInputStream(input).mkString
    decode[Filter](inputString) match {
      case Left(_) => throw new Exception("No feature file name provided")
      case Right(filter) => val exitStatus = Main.run(s"classpath:features/${filter.feature}")
        if(exitStatus != 0x0) {
          throw new Exception("Tests have failed")
        }
    }

  }
}
