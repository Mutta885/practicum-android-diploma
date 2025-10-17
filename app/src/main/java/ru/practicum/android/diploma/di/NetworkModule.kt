package ru.practicum.android.diploma.di

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.data.network.HhApi
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.data.network.RetrofitNetworkClient
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 30L

val networkModule = module {
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()

            val requestBuilder = originalRequest.newBuilder()
                .header("User-Agent", "DiplomaApp/1.0")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer ${BuildConfig.API_ACCESS_TOKEN}")

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://practicum-diploma-8bc38133faba.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    single { get<Retrofit>().create(HhApi::class.java) }

    factory<ConnectivityManager> {
        androidContext().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
    }

    factory <NetworkClient> {
        RetrofitNetworkClient(get(), get(), get())
    }
}
