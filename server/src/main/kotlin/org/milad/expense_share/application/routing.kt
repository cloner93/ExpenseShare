package org.milad.expense_share.application

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.milad.expense_share.data.repository.InMemoryFriendRepository
import org.milad.expense_share.data.repository.InMemoryGroupRepository
import org.milad.expense_share.data.repository.InMemoryTransactionRepository
import org.milad.expense_share.data.repository.InMemoryUserRepository
import org.milad.expense_share.domain.service.AuthService
import org.milad.expense_share.domain.service.FriendsService
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.domain.service.TransactionService
import org.milad.expense_share.presentation.auth.authRoutes
import org.milad.expense_share.presentation.friends.friendRoutes
import org.milad.expense_share.presentation.groups.groupsRoutes

internal fun Application.routing() {
    val userRepository = InMemoryUserRepository()
    val groupRepository = InMemoryGroupRepository()
    val transactionRepository = InMemoryTransactionRepository()
    val friendRepository = InMemoryFriendRepository()

    routing {
        authRoutes(
            AuthService(userRepository)
        )
        groupsRoutes(
            GroupService(
                groupRepository = groupRepository,
                userRepository = userRepository,
                transactionRepository = transactionRepository
            ),
            TransactionService(transactionRepository)
        )
        friendRoutes(
            FriendsService(friendRepository)
        )
    }
}