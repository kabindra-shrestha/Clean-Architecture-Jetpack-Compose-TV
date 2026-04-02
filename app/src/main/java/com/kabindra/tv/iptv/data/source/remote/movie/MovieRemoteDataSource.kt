package com.kabindra.tv.iptv.data.source.remote.movie

import com.kabindra.tv.iptv.data.model.MovieCategoryDTO
import com.kabindra.tv.iptv.data.model.MovieDetailDTO
import com.kabindra.tv.iptv.utils.mock.mockMovieCategories
import com.kabindra.tv.iptv.utils.mock.mockMovieDetail
import kotlinx.coroutines.delay

interface MovieRemoteDataSource {
    suspend fun getMovieCategories(): List<MovieCategoryDTO>
    suspend fun getMovieDetail(movieId: String): MovieDetailDTO
}

class FakeMovieRemoteDataSource : MovieRemoteDataSource {
    override suspend fun getMovieCategories(): List<MovieCategoryDTO> {
        delay(450)
        return mockMovieCategories()
    }

    override suspend fun getMovieDetail(movieId: String): MovieDetailDTO {
        delay(350)
        return mockMovieDetail(movieId)
    }
}
