package com.kabindra.tv.iptv.presentation.ui.screen.login

import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.MediaCacheStatus
import com.kabindra.tv.iptv.utils.constants.ConfirmationType
import com.kabindra.tv.iptv.utils.constants.ResponseType

data class LoginState(
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
    val isLogged: Boolean? = false,
    val user: User? = null,
    val liveTVCacheStatus: MediaCacheStatus = MediaCacheStatus.Checking,
    val liveTVStatusMessage: String = "Checking live TV data...",
    val liveTVSyncErrorMessage: String = "",
    val liveTVCategoryCount: Int = 0,
    val liveTVChannelCount: Int = 0,
    val hasLiveTVData: Boolean = false,
    val isLiveTVSyncing: Boolean = false,
    val movieCacheStatus: MediaCacheStatus = MediaCacheStatus.Checking,
    val movieStatusMessage: String = "Checking movie data...",
    val movieSyncErrorMessage: String = "",
    val movieCategoryCount: Int = 0,
    val movieCount: Int = 0,
    val hasMovieData: Boolean = false,
    val isMovieSyncing: Boolean = false,
)
