package org.milad.expense_share.application

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.milad.expense_share.data.repository.InMemoryFriendRepository
import org.milad.expense_share.data.repository.InMemoryGroupRepository
import org.milad.expense_share.data.repository.InMemoryTransactionRepository
import org.milad.expense_share.data.repository.InMemoryUserRepository
import org.milad.expense_share.domain.service.AuthService
import org.milad.expense_share.domain.service.FriendService
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.domain.service.TransactionService
import org.milad.expense_share.presentation.authRoutes
import org.milad.expense_share.presentation.friendRoutes
import org.milad.expense_share.presentation.groupsRoutes

internal fun Application.routing() {
    routing {
        authRoutes(AuthService(InMemoryUserRepository()))
        groupsRoutes(
            GroupService(InMemoryGroupRepository()),
            TransactionService(InMemoryTransactionRepository())
        )
        friendRoutes(FriendService(InMemoryFriendRepository()))
    }
}