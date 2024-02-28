package com.example.awsbusket1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

class MainActivity: AppCompatActivity() {
    private val TAG = "MainActivity"
    private val AWS_ACCESS_KEY = "AKIAZT3YGBLJTJ7XU66E"
    private val AWS_SECRET_KEY = "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"
    private val BUCKET_NAME = "aidoctorsportal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch(Dispatchers.IO) {
            uploadAudioFileAndGetURL(R.raw.sample3, "audio_filetest2.mp3")
        }
    }

    // Uploads a file to S3 and returns the pre-signed URL
    private fun uploadAudioFileAndGetURL(rawResourceId: Int, objectKey: String) {
        val uniqueFileName = generateRandomFileName() + ".mp3"
        // Copy raw resource to app's cache directory
        val file = copyRawResourceToFile(rawResourceId, objectKey)

        file?.let {
            // Upload file to S3
            val awsCredentials = BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
            val s3Client: AmazonS3 = AmazonS3Client(awsCredentials)
            s3Client.putObject(PutObjectRequest(BUCKET_NAME, uniqueFileName, file).withCannedAcl(
                CannedAccessControlList.PublicRead))


//presigned utl
            val generatePresignedUrlRequest = GeneratePresignedUrlRequest(BUCKET_NAME, uniqueFileName)
            Log.d("S3Upload", "expiretion: $generatePresignedUrlRequest")
            val url: URL = s3Client.generatePresignedUrl(generatePresignedUrlRequest)
           Log.d("S3Upload", "Pre-signed URL for the uploaded audio file: $url")
        } ?: run {
            Log.e("S3Upload", "Error copying raw resource to file")
        }
    }
    private fun generateRandomFileName(): String {
        // Generate a random file name using alphanumeric characters
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..10).map { allowedChars.random() }.joinToString("")
    }
    // Copy raw resource to a file in the app's cache directory
    private fun copyRawResourceToFile(rawResourceId: Int, fileName: String): File? {
        return try {
            // Open the raw resource
            val inputStream: InputStream = resources.openRawResource(rawResourceId)

            // Create a file in the app's cache directory
            val file = File(cacheDir, fileName)

            // Copy the contents of the raw resource to the file
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.close()
            inputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}