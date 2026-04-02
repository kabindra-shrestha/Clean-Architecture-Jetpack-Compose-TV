package com.kabindra.tv.iptv.data.source.remote.movies

import com.kabindra.tv.iptv.data.model.media.MovieCategoryDto
import com.kabindra.tv.iptv.data.model.media.MovieDetailDto
import com.kabindra.tv.iptv.utils.mock.mockMovieCategories
import com.kabindra.tv.iptv.utils.mock.mockMovieDetail
import kotlinx.coroutines.delay

interface MoviesRemoteDataSource {
    suspend fun getMovieCategories(): List<MovieCategoryDto>
    suspend fun getMovieDetail(movieId: String): MovieDetailDto
}

class FakeMoviesRemoteDataSource : MoviesRemoteDataSource {
    override suspend fun getMovieCategories(): List<MovieCategoryDto> {
        delay(450)
        return mockMovieCategories()
    }

    override suspend fun getMovieDetail(movieId: String): MovieDetailDto {
        delay(350)
        return mockMovieDetail(movieId)
    }
}
