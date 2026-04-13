package com.kabindra.tv.iptv.domain.repository.xtream.movie

import com.kabindra.tv.iptv.utils.ktor.Result
import io.github.saifullah.xtream.model.XtreamCategory
import io.github.saifullah.xtream.model.XtreamMovie
import io.github.saifullah.xtream.model.XtreamMovieDetail
import kotlinx.coroutines.flow.Flow

interface MovieXtreamRepository {
    suspend fun getMovies(): Flow<Result<List<XtreamMovie>>>
    suspend fun getMovieCategories(): Flow<Result<List<XtreamCategory>>>
    suspend fun getMovieDetail(streamId: Long): Flow<Result<XtreamMovieDetail>>
}