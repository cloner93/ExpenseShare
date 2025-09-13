package org.milad.expense_share.presentation.friends.model

import kotlinx.serialization.Serializable
import org.milad.expense_share.data.models.User

@Serializable
data class RequestsResponse(
    val incoming: List<User>,
    val outgoing: List<User>
)