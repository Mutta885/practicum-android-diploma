package ru.practicum.android.diploma.domain.usecase

import android.util.Log
import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.api.GetIndustriesUseCase
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.domain.repository.DataRepository
import java.io.IOException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class GetIndustriesUseCaseImpl(
    private val repository: DataRepository
) : GetIndustriesUseCase{

    override fun execute(): Flow<Result<List<Industry>?>> {
        return repository.getIndustries()
    }
}
