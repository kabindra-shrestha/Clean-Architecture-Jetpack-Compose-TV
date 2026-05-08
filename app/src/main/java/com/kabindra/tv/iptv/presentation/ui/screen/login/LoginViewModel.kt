package com.kabindra.tv.iptv.presentation.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.data.request.LoginUserDataRequest
import com.kabindra.tv.iptv.domain.usecase.room.LoginRoomUseCase
import com.kabindra.tv.iptv.utils.constants.ResponseType
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRoomUseCase: LoginRoomUseCase
) : ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())

    val loginState = _loginState
        .onStart { }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LoginState()
        )

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.GetLogin -> {
                getLoginUser(event.loginUserDataRequest)
            }

            is LoginEvent.GetIsLogged -> {
            }

            is LoginEvent.GetUser -> {
                getUser()
            }
        }
    }

    init {
        getUser()
    }

    fun getLoginUser(loginCheckDataRequest: LoginUserDataRequest) {
        viewModelScope.launch {
            loginRoomUseCase.executeGetLoginUser(loginCheckDataRequest).collect { result ->
                when (result) {
                    is Result.Initial -> Unit

                    is Result.Loading -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = true
                        )
                    }

                    is Result.Success -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            isLogged = result.data.server_name?.isNotEmpty() == true,
                            user = result.data
                        )
                    }

                    is Result.Error -> {
                        _loginState.value = _loginState.value.copy(
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

    fun getUser() {
        viewModelScope.launch {
            loginRoomUseCase.executeGetUser().collect { result ->
                when (result) {
                    is Result.Initial -> Unit

                    is Result.Loading -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = true
                        )
                    }

                    is Result.Success -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            user = result.data
                        )
                    }

                    is Result.Error -> {
                        _loginState.value = _loginState.value.copy(
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
        _loginState.value = LoginState()
    }
}