package org.milad.expense_share

import model.Group
import model.Transaction
import model.TransactionStatus
import model.User
import kotlin.random.Random

object FakeDataSource {

    private val users = listOf(
        User(1, "Milad", "+491520000001"),
        User(2, "Armin", "+491520000002"),
        User(3, "Sara", "+491520000003"),
        User(4, "Nima", "+491520000004"),
    )

    private val transactions = listOf(
        Transaction(
            id = 1,
            groupId = 1,
            title = "Dinner at Le Jules Verne",
            amount = 150.0,
            description = "Restaurant bill shared among group members",
            createdBy = 1,
            status = TransactionStatus.APPROVED,
            createdAt = System.currentTimeMillis() - 86_400_000L, // 1 day ago
            transactionDate = System.currentTimeMillis() - 86_400_000L / 2,
            approvedBy = 2
        ),
        Transaction(
            id = 2,
            groupId = 1,
            title = "Louvre Museum tickets",
            amount = 50.0,
            description = "Entry tickets for everyone",
            createdBy = 2,
            status = TransactionStatus.APPROVED,
            createdAt = System.currentTimeMillis() - 172_800_000L, // 2 days ago
            transactionDate = System.currentTimeMillis() - 172_800_000L / 2,
            approvedBy = 1
        ),
        Transaction(
            id = 3,
            groupId = 1,
            title = "Hotel Booking",
            amount = 300.0,
            description = "Hotel for 2 nights",
            createdBy = 3,
            status = TransactionStatus.PENDING,
            createdAt = System.currentTimeMillis() - 259_200_000L, // 3 days ago
            transactionDate = System.currentTimeMillis() - 259_200_000L / 2
        ),
        Transaction(
            id = 4,
            groupId = 2,
            title = "Train Tickets",
            amount = 75.0,
            description = "Train from Berlin to Munich",
            createdBy = 2,
            status = TransactionStatus.REJECTED,
            createdAt = System.currentTimeMillis() - 100_000_000L,
            transactionDate = System.currentTimeMillis() - 90_000_000L
        ),
    )
    private val groups = listOf(
        Group(1, "Trip to Paris", ownerId = 1, members = users, transactions = transactions),
        Group(2, "Weekend Getaway", ownerId = 2, members = users.subList(0,1), transactions = transactions.subList(0,1)),
        Group(3, "Ski Trip", ownerId = 3, members = users.subList(2,3), transactions = transactions)
    )


    // --- Public accessors for testing ---

    fun getAllUsers(): List<User> = users

    fun getAllGroups(): List<Group> = groups

    fun getAllTransactions(): List<Transaction> = transactions

    fun getTransactionsByGroup(groupId: Int?): List<Transaction> {

        return if (groupId != null) {
            transactions.filter { it.groupId == groupId }
        } else
            emptyList()
    }

    fun getTransactionsByStatus(status: TransactionStatus): List<Transaction> =
        transactions.filter { it.status == status }

    fun getGroupById(groupId: Int): Group? =
        groups.find { it.id == groupId }

    fun getUserById(userId: Int): User? =
        users.find { it.id == userId }

    fun createRandomTransaction(groupId: Int, createdBy: Int): Transaction {
        val id = transactions.maxOfOrNull { it.id }?.plus(1) ?: 1
        val titles = listOf("Dinner", "Tickets", "Snacks", "Gas", "Coffee", "Museum")
        val title = titles.random()
        return Transaction(
            id = id,
            groupId = groupId,
            title = title,
            amount = Random.nextDouble(10.0, 200.0),
            description = "Generated transaction for testing",
            createdBy = createdBy,
            status = TransactionStatus.entries.random(),
            createdAt = System.currentTimeMillis(),
            transactionDate = System.currentTimeMillis()
        )
    }
}