package ru.practicum.android.diploma.data.network

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
                emit(Response().apply { resultCode = -1 })
            } else {
                try {
                    when (dto) {
                        is FilterAreasRequest -> {
                            val list = api.getAreas()
                            if (list.code() == 200) {
                                val result =
                                    list.body()?.let { FilterAreasResponse(it) } ?: FilterAreasResponse(listOf())
                                emit(result.apply { resultCode = 200 })
                            } else {
                                emit(Response().apply { resultCode = list.code() })
                            }
                        }

                        is IndustryRequest -> {
                            val list = api.getIndustries()
                            if (list.code() == 200) {
                                val result = list.body()?.let { IndustryResponse(it) } ?: IndustryResponse(listOf())
                                emit(result.apply { resultCode = 200 })
                            } else {
                                emit(Response().apply { resultCode = list.code() })
                            }
                        }

                        else -> {
                            emit(Response().apply { resultCode = 400 })
                        }
                    }
                } catch (e: Throwable) {
                    emit(Response().apply { resultCode = 500 })
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
