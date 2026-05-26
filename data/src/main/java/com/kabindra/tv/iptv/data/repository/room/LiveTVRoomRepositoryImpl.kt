package com.kabindra.tv.iptv.data.repository.room

import com.kabindra.tv.iptv.data.model.LiveTVDTO
import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.source.room.AppDatabase
import com.kabindra.tv.iptv.data.source.xtream.livetv.LiveTVXtreamDataSource
import com.kabindra.tv.iptv.domain.entity.LiveTV
import com.kabindra.tv.iptv.domain.entity.LiveTVCategory
import com.kabindra.tv.iptv.domain.entity.LiveTVSyncSummary
import com.kabindra.tv.iptv.domain.repository.room.LiveTVRoomRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

private const val LIVE_TV_PAGE_SIZE = 500

class LiveTVRoomRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val xtreamDataSource: LiveTVXtreamDataSource,
) : LiveTVRoomRepository {

    override suspend fun hasLiveTVData(): Boolean {
        return appDatabase.liveTVDao.hasLiveTVData()
    }

    override fun observeLiveTVCategories(): Flow<Result<List<LiveTVCategory>>> {
        return appDatabase.liveTVDao.observeCategories()
            .map { categories ->
                Result.Success(categories.map { it.toDomain() }) as Result<List<LiveTVCategory>>
            }
            .onStart { emit(Result.Loading) }
            .catch { throwable ->
                emit(Result.Error(ResultError.parseException(throwable.toException())))
            }
    }

    override fun observeLiveTVChannels(): Flow<Result<List<LiveTV>>> {
        return appDatabase.liveTVDao.observeChannels()
            .map { channels ->
                Result.Success(channels.map { it.toDomain() }) as Result<List<LiveTV>>
            }
            .onStart { emit(Result.Loading) }
            .catch { throwable ->
                emit(Result.Error(ResultError.parseException(throwable.toException())))
            }
    }

    override fun syncLiveTVContent(): Flow<Result<LiveTVSyncSummary>> = flow {
        emit(Result.Loading)

        try {
            val categories = xtreamDataSource.getLiveTVCategories()
                .filter { it.category_id.isNotBlank() }
                .distinctBy { it.category_id }
            val channels = fetchAllChannels()

            appDatabase.liveTVDao.replaceAll(
                categories = categories,
                channels = channels,
            )

            emit(
                Result.Success(
                    LiveTVSyncSummary(
                        categoryCount = categories.size,
                        channelCount = channels.size,
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }

    private suspend fun fetchAllChannels(): List<LiveTVDTO> {
        val channels = linkedMapOf<Int, LiveTVDTO>()
        var offset = 0

        while (true) {
            val page = xtreamDataSource.getLiveTVChannels(
                offset = offset,
                itemsPerPage = LIVE_TV_PAGE_SIZE,
            )

            if (page.isEmpty()) break

            val previousSize = channels.size
            page.filter { it.stream_id > 0 }
                .forEach { channel ->
                    channels[channel.stream_id] = channel
                }

            val addedCount = channels.size - previousSize
            if (page.size < LIVE_TV_PAGE_SIZE || addedCount == 0) break

            offset += LIVE_TV_PAGE_SIZE
        }

        return channels.values.toList()
    }

    private fun Throwable.toException(): Exception {
        return this as? Exception ?: Exception(this)
    }
}
