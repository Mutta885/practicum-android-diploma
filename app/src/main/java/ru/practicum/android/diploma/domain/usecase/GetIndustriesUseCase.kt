package ru.practicum.android.diploma.domain.usecase

import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.DataRepository
import java.io.IOException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class GetIndustriesUseCase(private val repository: DataRepository) {

    suspend operator fun invoke(): Result<List<Industry>> {
        return try {
            val industries = repository.getIndustries()
            Result.success(industries)
        } catch (e: UnknownHostException) {
            Result.failure(IOException("Проверьте подключение к интернету", e))
        } catch (e: SSLHandshakeException) {
            Result.failure(IOException("Ошибка безопасности соединения", e))
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}
