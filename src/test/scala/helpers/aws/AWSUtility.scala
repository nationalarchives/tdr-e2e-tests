package helpers.aws
import java.nio.file.Path
import com.typesafe.config.{Config, ConfigFactory}
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, DefaultCredentialsProvider}
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, PutObjectRequest, PutObjectResponse}
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.utils.SdkAutoCloseable

import scala.util.Try

class AWSUtility {

  val config: Config = ConfigFactory.load
  val httpClient: SdkHttpClient = ApacheHttpClient.builder.build

  val provider: AwsCredentialsProvider with SdkAutoCloseable = if(config.hasPath("s3.role")) {
    //Assume role if running on Jenkins
    val sts = StsClient.builder()
      .region(Region.EU_WEST_2)
      .httpClient(httpClient).build()

    StsAssumeRoleCredentialsProvider.builder().stsClient(sts)
      .refreshRequest(
        AssumeRoleRequest.builder()
          .roleArn(config.getString("s3.role"))
          .roleSessionName(s"e2e-tests-session")
          .build())
      .build()
  } else {
    //Otherwise, use local credentials
    DefaultCredentialsProvider.builder().reuseLastProviderEnabled(false).build()
  }

  val s3: S3Client = S3Client.builder()
    .credentialsProvider(provider)
    .httpClient(httpClient)
    .build()

  def isFileInS3(bucket: String, key: String): Boolean =
    Try(s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())).isSuccess

  def uploadFileToS3(bucket: String, key: String, filePath: Path): PutObjectResponse =
    s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), filePath)
}

object AWSUtility {
  def apply(): AWSUtility = new AWSUtility()
}
