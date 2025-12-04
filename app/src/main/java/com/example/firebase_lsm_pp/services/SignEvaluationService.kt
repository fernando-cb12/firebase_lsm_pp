package com.example.firebase_lsm_pp.services

import kotlinx.coroutines.delay
import java.io.File

data class SignEvaluationResponse(
    val result: Boolean,
    val confidence: Double,
    val hints: List<String>,
    val top_k: List<Int>
)

class SignEvaluationService {
    suspend fun evaluateSign(videoFile: File): Result<SignEvaluationResponse> {
        // Simular delay de red
        delay(2000)
        
        // Mock response matching user's structure
        return Result.success(
            SignEvaluationResponse(
                result = false,
                confidence = 0.05,
                hints = listOf(
                    "Revisa la posición de la mano derecha (altura/orientación).",
                    "Revisa la posición de la mano izquierda (altura/orientación)."
                ),
                top_k = listOf(4, 8, 10)
            )
        )
    }
}
