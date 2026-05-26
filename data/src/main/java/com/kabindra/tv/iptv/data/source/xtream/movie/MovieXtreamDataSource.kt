package com.kabindra.tv.iptv.data.source.xtream.movie

import com.kabindra.tv.iptv.data.model.MovieCategoryDTO
import com.kabindra.tv.iptv.data.model.MovieDTO
import com.kabindra.tv.iptv.data.model.MovieDetailDTO
import com.kabindra.tv.iptv.data.source.xtream.XtreamService
import com.kabindra.tv.iptv.domain.repository.session.CurrentUserRepository

interface MovieXtreamDataSource {
    suspend fun getMovies(
        offset: Int? = null,
        itemsPerPage: Int? = null,
    ): List<MovieDTO>

    suspend fun getMovieCategories(): List<MovieCategoryDTO>
    suspend fun getMovieDetail(streamId: Long): MovieDetailDTO
}

class MovieXtreamDataSourceImpl(
    private val xtreamService: XtreamService,
    private val userCredentialsProvider: CurrentUserRepository
) : MovieXtreamDataSource {

    override suspend fun getMovies(
        offset: Int?,
        itemsPerPage: Int?,
    ): List<MovieDTO> {
        val user = userCredentialsProvider.getCurrentUser()
            ?: throw IllegalStateException("User not logged in")

        return xtreamService.getMovies(
            serverName = user.server_name ?: throw IllegalStateException("Server name not found"),
            username = user.username ?: throw IllegalStateException("Username not found"),
            password = user.password ?: throw IllegalStateException("Password not found"),
            offset = offset,
            itemsPerPage = itemsPerPage,
        )
    }

    override suspend fun getMovieCategories(): List<MovieCategoryDTO> {
        val user = userCredentialsProvider.getCurrentUser()
            ?: throw IllegalStateException("User not logged in")

        return xtreamService.getMovieCategories(
            serverName = user.server_name ?: throw IllegalStateException("Server name not found"),
            username = user.username ?: throw IllegalStateException("Username not found"),
            password = user.password ?: throw IllegalStateException("Password not found")
        )
    }

    override suspend fun getMovieDetail(streamId: Long): MovieDetailDTO {
        val user = userCredentialsProvider.getCurrentUser()
            ?: throw IllegalStateException("User not logged in")

        return xtreamService.getMovieDetail(
            serverName = user.server_name ?: throw IllegalStateException("Server name not found"),
            username = user.username ?: throw IllegalStateException("Username not found"),
            password = user.password ?: throw IllegalStateException("Password not found"),
            streamId = streamId
        )
    }

}
