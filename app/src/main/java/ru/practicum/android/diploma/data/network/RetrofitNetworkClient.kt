package ru.practicum.android.diploma.data.network

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.IOException
import ru.practicum.android.diploma.data.dto.FilterAreasRequest
import ru.practicum.android.diploma.data.dto.FilterAreasResponse
import ru.practicum.android.diploma.data.dto.IndustryRequest
import ru.practicum.android.diploma.data.dto.IndustryResponse
import ru.practicum.android.diploma.data.dto.Response

class RetrofitNetworkClient(
    private val api: HhApi,
    private val connectivityManager: ConnectivityManager
) : NetworkClient {
    override suspend fun doRequest(dto: Any): Flow<Response> {
        return flow {
            if (!isConnected()) {
                emit(Response().apply { resultCode = HTTP_ERROR_NOT_INTERNET })
            } else {
                try {
                    when (dto) {
                        is FilterAreasRequest -> {
                            emit(filterAreaRequest())
                        }

                        is IndustryRequest -> {
                            emit(industriesRequest())
                        }

                        else -> {
                            emit(Response().apply { resultCode = HTTP_ERROR_ALL })
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG_ERROR, e.message.toString())
                    emit(Response().apply { resultCode = HTTP_ERROR_SERVER })
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun industriesRequest(): Response {
        val list = api.getIndustries()
        if (list.code() == HTTP_OK) {
            val result = list.body()?.let { IndustryResponse(it) } ?: IndustryResponse(listOf())
            return result.apply { resultCode = if (list.body()?.isNotEmpty() == true) HTTP_OK else HTTP_OK_EMPTY }
        } else {
            return Response().apply { resultCode = list.code() }
        }
    }

    private suspend fun filterAreaRequest(): Response {
        val list = api.getAreas()
        if (list.code() == HTTP_OK) {
            val result = list.body()?.let { FilterAreasResponse(it) } ?: FilterAreasResponse(listOf())
            return result.apply { resultCode = if (list.body()?.isNotEmpty() == true) HTTP_OK else HTTP_OK_EMPTY }
        } else {
            return Response().apply { resultCode = list.code() }
        }
    }

    private fun isConnected(): Boolean {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
        return false
    }

    companion object {
        private const val HTTP_ERROR_NOT_INTERNET = -1
        private const val HTTP_OK = 200
        private const val HTTP_OK_EMPTY = -2
        private const val HTTP_ERROR_SERVER = 500
        private const val HTTP_ERROR_ALL = 400
        private const val TAG_ERROR = "error"
    }
}
