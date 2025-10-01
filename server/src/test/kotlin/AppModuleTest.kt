import io.kotest.core.spec.style.DescribeSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.koin.test.KoinTest
import org.koin.test.inject
import org.milad.expense_share.di.appModule
import org.milad.expense_share.domain.repository.FriendRepository
import org.milad.expense_share.domain.repository.GroupRepository
import org.milad.expense_share.domain.repository.TransactionRepository
import org.milad.expense_share.domain.repository.UserRepository
import org.milad.expense_share.domain.service.AuthService
import org.milad.expense_share.domain.service.FriendsService
import org.milad.expense_share.domain.service.GroupService
import org.milad.expense_share.domain.service.TransactionService

class AppModuleTest : KoinTest, DescribeSpec() {
    init {
        extension(KoinExtension(module = appModule))

        describe("Repository injection") {
            it("FriendRepository should be injected correctly") {
                val repo: FriendRepository by inject()
                repo shouldNotBe null
            }
            it("UserRepository should be injected correctly") {
                val repo: UserRepository by inject()
                repo shouldNotBe null
            }
            it("GroupRepository should be injected correctly") {
                val repo: GroupRepository by inject()
                repo shouldNotBe null
            }
            it("TransactionRepository should be injected correctly") {
                val repo: TransactionRepository by inject()
                repo shouldNotBe null
            }
            it("All repositories should be single instances") {
                val authRepo1: UserRepository by inject()
                val authRepo2: UserRepository by inject()
                authRepo1 shouldBe authRepo2
            }
        }

        describe("Service injection") {
            it("AuthService should be injected correctly") {
                val repo by inject<AuthService>()
                repo shouldNotBe null
            }
            it("FriendsService should be injected correctly") {
                val repo by inject<FriendsService>()
                repo shouldNotBe null
            }
            it("GroupService should be injected correctly") {
                val repo by inject<GroupService>()
                repo shouldNotBe null
            }
            it("TransactionService should be injected correctly") {
                val repo by inject<TransactionService>()
                repo shouldNotBe null
            }
            it("All services should be single instances") {
                val authRepo1 by inject<AuthService>()
                val authRepo2 by inject<AuthService>()
                authRepo1 shouldBe authRepo2
            }
        }
    }
}