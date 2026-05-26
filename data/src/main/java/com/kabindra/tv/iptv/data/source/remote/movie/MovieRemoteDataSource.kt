package com.kabindra.tv.iptv.data.source.remote.movie

import com.kabindra.tv.iptv.data.model.VODCategoryDTO
import com.kabindra.tv.iptv.data.model.VODDetailDTO
import com.kabindra.tv.iptv.utils.mock.mockMovieCategories
import com.kabindra.tv.iptv.utils.mock.mockMovieDetail
import kotlinx.coroutines.delay

interface MovieRemoteDataSource {
    suspend fun getMovieCategories(): List<VODCategoryDTO>
    suspend fun getMovieDetail(movieId: String): VODDetailDTO
}

class FakeMovieRemoteDataSource : MovieRemoteDataSource {
    override suspend fun getMovieCategories(): List<VODCategoryDTO> {
        delay(450)
        return mockMovieCategories()
    }

    override suspend fun getMovieDetail(movieId: String): VODDetailDTO {
        delay(350)
        return mockMovieDetail(movieId)
    }
}
