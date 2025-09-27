import di.dataAggregator
import io.kotest.core.spec.style.StringSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import repository.AuthRepository
import repository.FriendsRepository
import repository.GroupsRepository
import repository.TransactionsRepository

import org.koin.test.KoinTest
import org.koin.test.inject

class DataModuleTest : KoinTest, StringSpec() {
    init {
        extension(KoinExtension(module = dataAggregator))

        "AuthRepository should be injected correctly" {
            val repo: AuthRepository by inject()
            repo shouldNotBe null
        }

        "FriendsRepository should be injected correctly" {
            val repo: FriendsRepository by inject()
            repo shouldNotBe null
        }

        "GroupsRepository should be injected correctly" {
            val repo: GroupsRepository by inject()
            repo shouldNotBe null
        }

        "TransactionsRepository should be injected correctly" {
            val repo: TransactionsRepository by inject()
            repo shouldNotBe null
        }

        "All repositories should be single instances" {
            val authRepo1: AuthRepository by inject()
            val authRepo2: AuthRepository by inject()
            authRepo1 shouldBe authRepo2
        }
    }
}
