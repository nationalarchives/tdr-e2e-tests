package helpers.aws
import com.typesafe.config.ConfigFactory
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, ListObjectsRequest}
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider

import scala.util.Try

class AWSUtility {

  def isFileInS3(bucket: String, key: String): Boolean = {
    val config = ConfigFactory.load
    val httpClient = ApacheHttpClient.builder.build

    val provider = if(config.hasPath("s3.role")) {
      //Assume role if running on Jenkins
      val sts = StsClient.builder()
        .region(Region.EU_WEST_2)
        .httpClient(httpClient).build()

      StsAssumeRoleCredentialsProvider.builder().stsClient(sts)
        .refreshRequest(
          AssumeRoleRequest.builder()
            .roleArn(config.getString("s3.role"))
            .build())
        .build()
    } else {
      //Otherwise, use local credentials
      DefaultCredentialsProvider.builder().build()
    }

    val s3 = S3Client.builder()
      .credentialsProvider(provider)
      .httpClient(httpClient)
      .build()

    Try(s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build())).isSuccess
  }
}

object AWSUtility {
  def apply(): AWSUtility = new AWSUtility()
}
