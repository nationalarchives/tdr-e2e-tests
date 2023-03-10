package helpers.aws
import java.nio.file.Path

import com.typesafe.config.{Config, ConfigFactory}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, ContainerCredentialsProvider, DefaultCredentialsProvider}
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{HeadObjectRequest, PutObjectRequest, PutObjectResponse}
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.utils.SdkAutoCloseable

import scala.util.Try

class AWSUtility {

  lazy val config: Config = ConfigFactory.load
  lazy val httpClient: SdkHttpClient = ApacheHttpClient.builder.build

  val s3: S3Client = S3Client.builder()
    .httpClient(httpClient)
    .build()

  def isFileInS3(bucket: String, key: String): Boolean =
    Try(s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build())).isSuccess

  def uploadFileToS3(bucket: String, key: String, filePath: Path): PutObjectResponse =
    s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), filePath)
}

object AWSUtility {
  def apply(): AWSUtility = new AWSUtility()
}
