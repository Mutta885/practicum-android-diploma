package ru.practicum.android.diploma.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.data.dto.IndustryDto
import ru.practicum.android.diploma.data.dto.IndustryResponse
import ru.practicum.android.diploma.data.dto.Response

class RetrofitNetworkClient(
    private val api: HhApi,
    private val connectivityManager: ConnectivityManager,
    private val appContext: Context
) : NetworkClient {
    override suspend fun doRequest(dto: Any): Response = withContext(Dispatchers.IO) {
        if (!isConnected()) {
            Response().apply { resultCode = -1 }
        } else {
            try {
                if (dto is IndustryResponse) {
                    val result = api.getIndustriesResponse()
                    Response().apply { resultCode = 200 }
                }
                Response().apply { resultCode = 400 }
            } catch (e: Throwable) {
                Response().apply { resultCode = 500 }
            }
        }
    }

    override fun doIndustry(): Flow<Result<List<IndustryDto>>> {
        return flow {
            if (!isConnected()) {
                emit(Result.failure(Throwable(appContext.getString(R.string.error_network))))
            } else {
                try {
                    val result = api.getIndustriesResponse()
                    emit(Result.success(result))
                } catch (e: Throwable) {
                    emit(Result.failure(Throwable(appContext.getString(R.string.error_server))))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun isConnected(): Boolean {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
            }
        }
        return false
    }
}
