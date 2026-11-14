package org.milad.expense_share.profile

import androidx.lifecycle.viewModelScope
import com.pmb.common.theme.ThemeMode
import com.pmb.common.theme.ThemeStateHolder
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.launch
import model.User
import usecase.user.GetUserInfoUseCase

class ProfileViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val themeStateHolder: ThemeStateHolder,
) : BaseViewModel<ProfileAction, ProfileState, ProfileEvent>(
    initialState = ProfileState()
) {
    init {
        loadProfile()
        observeTheme()
    }

    override fun handle(action: ProfileAction) {
        when (action) {
            ProfileAction.Refresh -> loadProfile()
            is ProfileAction.SelectThemeMode -> themeStateHolder.setThemeMode(action.mode)
            ProfileAction.CopyPhone -> handleCopyPhone()
            ProfileAction.RequestLogout -> setState { it.copy(showLogoutConfirm = true) }
            ProfileAction.DismissLogout -> setState { it.copy(showLogoutConfirm = false) }
            ProfileAction.ConfirmLogout -> {
                setState { it.copy(showLogoutConfirm = false) }
                postEvent(ProfileEvent.LoggedOut)
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val user = getUserInfoUseCase()
            setState { it.copy(user = user, isLoading = false) }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeStateHolder.themeMode.collect { mode ->
                setState { it.copy(themeMode = mode) }
            }
        }
    }

    private fun handleCopyPhone() {
        val phone = viewState.value.user.phone
        if (phone.isNotBlank()) {
            postEvent(ProfileEvent.CopyPhoneToClipboard(phone))
        }
    }
}

data class ProfileState(
    val user: User = User(id = 0, username = "", phone = ""),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showLogoutConfirm: Boolean = false,
    val isLoading: Boolean = true,
) : BaseViewState

sealed interface ProfileAction : BaseViewAction {
    data object Refresh : ProfileAction
    data class SelectThemeMode(val mode: ThemeMode) : ProfileAction
    data object CopyPhone : ProfileAction
    data object RequestLogout : ProfileAction
    data object DismissLogout : ProfileAction
    data object ConfirmLogout : ProfileAction
}

sealed interface ProfileEvent : BaseViewEvent {
    data class CopyPhoneToClipboard(val phone: String) : ProfileEvent
    data object LoggedOut : ProfileEvent
}
