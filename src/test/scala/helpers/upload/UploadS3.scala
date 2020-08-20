package helpers

import java.io.{File, IOException}
import java.util.UUID

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest


object UploadS3 {

  @throws(classOf[IOException])
  def uploadToS3(s3: S3Client, identityId: String, consignmentId: UUID): Unit = {
    val testBucket: String = "tdr-upload-files-dirty-intg"

    val fileThree = new File(s"${System.getProperty("user.dir")}/src/test/resources/testfiles/largefile")

    s3.putObject(PutObjectRequest.builder().bucket(testBucket).key(s"E2E-TEST:eu-west-2:${identityId}/${consignmentId}/562fe3cd-31e7-41d0-a103-fdf59336e105")
      .build(),
      RequestBody.fromFile(fileThree)
    )
  }
}
