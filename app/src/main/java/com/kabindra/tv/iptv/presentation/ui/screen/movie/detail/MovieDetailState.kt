package com.kabindra.tv.iptv.presentation.ui.screen.movie.detail

import com.kabindra.tv.iptv.domain.entity.VODDetail
import com.kabindra.tv.iptv.domain.entity.VODSummary
import com.kabindra.tv.iptv.utils.constants.ConfirmationType
import com.kabindra.tv.iptv.utils.constants.ResponseType

data class MovieDetailState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val isEmpty: Boolean = false,
    val successType: ResponseType = ResponseType.None,
    val successMessage: String = "",
    val errorType: ResponseType = ResponseType.None,
    val errorStatusCode: Int = -1,
    val errorTitle: String = "",
    val errorMessage: String = "",
    val confirmationType: ConfirmationType = ConfirmationType.None,
    val confirmationMessage: String = "",
    val movie: VODDetail? = null,
    val recommendedMovies: List<VODSummary> = emptyList(),
    val currentMovieId: String? = null,
)
