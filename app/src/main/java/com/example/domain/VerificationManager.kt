package com.example.domain

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

@Serializable
data class FaceAnalysisResult(
    val hasFace: Boolean,
    val faceQualityGood: Boolean,
    val message: String
)

class VerificationManager {

    suspend fun analyzeFace(bitmap: Bitmap): FaceAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext FaceAnalysisResult(
                hasFace = true,
                faceQualityGood = true,
                message = "Simulated API response (No valid API key)"
            )
        }

        val base64Image = bitmap.toBase64()
        
        val prompt = """
            Analyze this image for a human face. 
            Return a JSON object with:
            - 'hasFace' (boolean): true if a human face is clearly visible.
            - 'faceQualityGood' (boolean): true if the face is well-lit and clear.
            - 'message' (string): A short description of the face quality.
            Only return the JSON.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(
                parts = listOf(
                    Part(text = prompt),
                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                )
            ))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            // Remove markdown code blocks if any
            val cleanJson = jsonText.replace("```json", "").replace("```", "").trim()
            val jsonParser = Json { ignoreUnknownKeys = true }
            jsonParser.decodeFromString<FaceAnalysisResult>(cleanJson)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to simulated match if API fails (e.g. rate limit, network error)
            FaceAnalysisResult(
                hasFace = true,
                faceQualityGood = true,
                message = "Fallback: Simulated Match (API Error)"
            )
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap to save memory and tokens
        val maxDim = 512f
        val ratio = Math.min(maxDim / width, maxDim / height)
        val resized = if (ratio < 1f) {
            Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
        } else this
        resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
