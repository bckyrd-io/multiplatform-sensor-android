package com.example.figcompose.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.figcompose.service.ApiService
import com.example.figcompose.service.CreateSessionRequest
import com.example.figcompose.service.CreateSessionResponse
import com.example.figcompose.service.RetrofitProvider
import com.example.figcompose.service.SessionDto
import com.example.figcompose.service.ReportDto
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

sealed class SessionDetailsState {
    object Idle : SessionDetailsState()
    object Loading : SessionDetailsState()
    data class Loaded(val session: SessionDto) : SessionDetailsState()
    data class Error(val message: String) : SessionDetailsState()
}

sealed class CreateSessionState {
    object Idle : CreateSessionState()
    object Submitting : CreateSessionState()
    data class Success(val sessionId: Int?) : CreateSessionState()
    data class Error(val message: String) : CreateSessionState()
}

sealed class ReportState {
    object Idle : ReportState()
    object Loading : ReportState()
    data class Loaded(val report: ReportDto) : ReportState()
    data class Error(val message: String) : ReportState()
}

sealed class SessionsListState {
    object Idle : SessionsListState()
    object Loading : SessionsListState()
    data class Loaded(val sessions: List<SessionDto>) : SessionsListState()
    data class Error(val message: String) : SessionsListState()
}

class SessionManager(private val context: Context) : ViewModel() {
    private val api: ApiService = RetrofitProvider.api()

    val detailState = mutableStateOf<SessionDetailsState>(SessionDetailsState.Idle)
    val createState = mutableStateOf<CreateSessionState>(CreateSessionState.Idle)
    val reportState = mutableStateOf<ReportState>(ReportState.Idle)
    val listState = mutableStateOf<SessionsListState>(SessionsListState.Idle)

    fun loadSession(sessionId: Int, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                detailState.value = SessionDetailsState.Loading
                val resp = api.getSession(sessionId)
                if (resp.isSuccessful) {
                    val session = resp.body()
                    if (session != null) {
                        detailState.value = SessionDetailsState.Loaded(session)
                        onComplete(true, null)
                    } else {
                        val msg = "Session not found"
                        detailState.value = SessionDetailsState.Error(msg)
                        onComplete(false, msg)
                    }
                } else {
                    val msg = parseErrorBody(resp.errorBody()?.string()).ifBlank { "Failed to load session" }
                    detailState.value = SessionDetailsState.Error(msg)
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "Unexpected error: ${e.message}"
                }
                detailState.value = SessionDetailsState.Error(msg)
                onComplete(false, msg)
            }
        }
    }

    fun loadSessions(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                listState.value = SessionsListState.Loading
                val resp = api.getSessions()
                if (resp.isSuccessful) {
                    val list = resp.body().orEmpty()
                    listState.value = SessionsListState.Loaded(list)
                    onComplete(true, null)
                } else {
                    val msg = parseErrorBody(resp.errorBody()?.string()).ifBlank { "Failed to load sessions" }
                    listState.value = SessionsListState.Error(msg)
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "Unexpected error: ${e.message}"
                }
                listState.value = SessionsListState.Error(msg)
                onComplete(false, msg)
            }
        }
    }

    fun createSession(
        name: String,
        type: String,
        date: String,
        startTime: String,
        endTime: String,
        location: String,
        notes: String,
        onComplete: (Boolean, Int?, String?) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                createState.value = CreateSessionState.Submitting
                val req = CreateSessionRequest(
                    sessionName = name,
                    sessionType = type,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    location = location.ifBlank { null },
                    notes = notes.ifBlank { null }
                )
                val resp = api.createSession(req)
                if (resp.isSuccessful) {
                    val body: CreateSessionResponse? = resp.body()
                    createState.value = CreateSessionState.Success(body?.sessionId)
                    onComplete(true, body?.sessionId, null)
                } else {
                    val msg = parseErrorBody(resp.errorBody()?.string()).ifBlank { "Failed to create session" }
                    createState.value = CreateSessionState.Error(msg)
                    onComplete(false, null, msg)
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "Unexpected error: ${e.message}"
                }
                createState.value = CreateSessionState.Error(msg)
                onComplete(false, null, msg)
            }
        }
    }

    fun loadReport(sessionId: Int, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                reportState.value = ReportState.Loading
                val resp = api.getReport(sessionId)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null) {
                        reportState.value = ReportState.Loaded(body)
                        onComplete(true, null)
                    } else {
                        val msg = "Report not available"
                        reportState.value = ReportState.Error(msg)
                        onComplete(false, msg)
                    }
                } else {
                    val msg = parseErrorBody(resp.errorBody()?.string()).ifBlank { "Failed to load report" }
                    reportState.value = ReportState.Error(msg)
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Server error: ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "Unexpected error: ${e.message}"
                }
                reportState.value = ReportState.Error(msg)
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
