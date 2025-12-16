package org.milad.expense_share.application

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.milad.expense_share.domain.service.AuthService
import org.milad.expense_share.domain.service.ChatService
import org.milad.expense_share.domain.service.FriendsService
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.domain.service.TransactionService
import org.milad.expense_share.presentation.auth.authRoutes
import org.milad.expense_share.presentation.chat.chatRoutes
import org.milad.expense_share.presentation.friends.friendRoutes
import org.milad.expense_share.presentation.groups.groupsRoutes
import org.milad.expense_share.presentation.transactions.transactionsRoutes

internal fun Application.configureRouting() {
    val authService by inject<AuthService>()
    val groupService by inject<GroupService>()
    val transactionService by inject<TransactionService>()
    val friendsService by inject<FriendsService>()
    val chatService by inject<ChatService>()  // NEW

    routing {
        authRoutes(authService)
        groupsRoutes(groupService)
        transactionsRoutes(transactionService)
        friendRoutes(friendsService)
        chatRoutes(chatService)  // NEW
    }
}