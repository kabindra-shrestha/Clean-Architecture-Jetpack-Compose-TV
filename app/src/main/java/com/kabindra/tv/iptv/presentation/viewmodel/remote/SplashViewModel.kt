package com.kabindra.tv.iptv.presentation.viewmodel.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.data.request.RefreshTokenDataRequest
import com.kabindra.tv.iptv.domain.usecase.remote.RefreshTokenUseCase
import com.kabindra.tv.iptv.utils.constants.ResponseType
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SplashViewModel(
    private val refreshTokenUseCase: RefreshTokenUseCase,
) : ViewModel() {
    private val _splashState = MutableStateFlow(SplashState())

    val splashState = _splashState
        .onStart { }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SplashState()
        )

    fun onEvent(event: SplashEvent) {
        when (event) {
            is SplashEvent.GetLoginRefreshUserDetails -> {
                getLoginRefreshUserDetails(event.refreshTokenDataRequest)
            }

            is SplashEvent.GetIsLogged -> {
            }

            is SplashEvent.GetUser -> {
            }
        }
    }

    fun getLoginRefreshUserDetails(refreshTokenDataRequest: RefreshTokenDataRequest) {
        viewModelScope.launch {
            refreshTokenUseCase.executeGetRefreshToken(refreshTokenDataRequest)
                .collect { result ->
                    when (result) {
                        is Result.Initial -> Unit
                        is Result.Loading -> {
                            _splashState.value =
                                _splashState.value.copy(
                                    isLoading = true
                                )
                        }

                        is Result.Success -> {
                            _splashState.value =
                                _splashState.value.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    successType = ResponseType.None,
                                    successMessage = "",
                                    refreshToken = result.data
                                )
                        }

                        is Result.Error -> {
                            _splashState.value =
                                _splashState.value.copy(
                                    isLoading = false,
                                    isError = true,
                                    errorType = ResponseType.None,
                                    errorStatusCode = result.error.statusCode,
                                    errorTitle = "",
                                    errorMessage = result.error.message
                                )
                        }
                    }
                }
        }
    }

    fun resetStates() {
        _splashState.value = SplashState()
    }
}