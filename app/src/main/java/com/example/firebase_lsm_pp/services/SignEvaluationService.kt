package com.example.firebase_lsm_pp.services

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

data class SignEvaluationResponse(
    val result: Boolean,
    val confidence: Double,
    val hints: List<String>,
    val top_k: List<Int>
)

class SignEvaluationService {
    private val api: SignApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(SignApiService::class.java)
    }

    suspend fun evaluateSign(videoFile: File, expectedWord: String): Result<SignEvaluationResponse> {
        android.util.Log.d("SignEvaluationService", "Sending expected_word: '$expectedWord'")
        
        val expectedWordInt = expectedWord.toIntOrNull()
        if (expectedWordInt == null) {
            return Result.failure(Exception("Error: expected_word debe ser un ID numérico, se recibió: '$expectedWord'"))
        }

        android.util.Log.d("SignEvaluationService", "Converted to int: $expectedWordInt")

        return try {
            val requestFile = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", videoFile.name, requestFile)
            
            // Create RequestBody for the integer value - FastAPI expects this as a form field
            val expectedWordBody = expectedWordInt.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.evaluateSign(expectedWordBody, body)

            android.util.Log.d("SignEvaluationService", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error details"
                android.util.Log.e("SignEvaluationService", "Error response: $errorBody")
                Result.failure(Exception("Error: ${response.code()} ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("SignEvaluationService", "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
