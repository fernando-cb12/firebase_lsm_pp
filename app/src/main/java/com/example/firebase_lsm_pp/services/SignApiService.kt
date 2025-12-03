package com.example.firebase_lsm_pp.services

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SignApiService {
    @Multipart
    @POST("evaluate-sign")
    suspend fun evaluateSign(
        @Part("expected_word") expectedWord: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<SignEvaluationResponse>
}
