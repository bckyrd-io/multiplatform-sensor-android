package com.example.figcompose.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.figcompose.service.ApiService
import com.example.figcompose.service.RetrofitProvider
import com.example.figcompose.service.SubmitSurveyRequest
import com.example.figcompose.service.SubmitSurveyResponse
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

sealed class SubmitSurveyState {
    object Idle : SubmitSurveyState()
    object Submitting : SubmitSurveyState()
    data class Success(val response: SubmitSurveyResponse?) : SubmitSurveyState()
    data class Error(val message: String) : SubmitSurveyState()
}

class SurveyManager(context: Context) : ViewModel() {
    private val api: ApiService = RetrofitProvider.api()
    val state = mutableStateOf<SubmitSurveyState>(SubmitSurveyState.Idle)

    fun submit(
        playerId: Int,
        sessionId: Int? = null,
        rating: Int? = null,
        condition: String? = null,
        performance: String? = null,
        notes: String? = null,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                state.value = SubmitSurveyState.Submitting
                val resp = api.submitSurvey(
                    SubmitSurveyRequest(
                        player_id = playerId,
                        session_id = sessionId,
                        rating = rating,
                        condition = condition,
                        performance = performance,
                        notes = notes,
                        response = null
                    )
                )
                if (resp.isSuccessful) {
                    state.value = SubmitSurveyState.Success(resp.body())
                    onComplete(true, null)
                } else {
                    val msg = parseErrorBody(resp.errorBody()?.string()).ifBlank { "Failed to submit feedback" }
                    state.value = SubmitSurveyState.Error(msg)
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "Unexpected error: ${e.message}"
                }
                state.value = SubmitSurveyState.Error(msg)
                onComplete(false, msg)
            }
        }
    }

    private fun parseErrorBody(body: String?): String {
        return try {
            if (body.isNullOrBlank()) return ""
            JSONObject(body).optString("error").ifBlank { body }
        } catch (ex: Exception) {
            body ?: ""
        }
    }
}
