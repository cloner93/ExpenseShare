package kotest
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.runBlocking
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
class UserManagerTest : StringSpec({

    val userManager = UserManager()

    "getUsersFlow should emit all users" {
        runBlocking {
            val result = userManager.getUsersFlow().toList()
            result.map { it.name } shouldContainExactly listOf("Alice", "Bob", "Charlie", "David")
        }
    }

    "getUsersStartingWith should filter users correctly" {
        runBlocking {
            val result = userManager.getUsersStartingWith('C').toList()
            result.map { it.name } shouldContainExactly listOf("Charlie")
        }
    }

    "getUsersStartingWith with no match should return empty list" {
        runBlocking {
            val result = userManager.getUsersStartingWith('Z').toList()
            result shouldContainExactly emptyList()
        }
    }
})

data class User(val id: Int, val name: String)

class UserManager {
    private val users = listOf(
        User(1, "Alice"),
        User(2, "Bob"),
        User(3, "Charlie"),
        User(4, "David")
    )

    fun getUsersFlow() = kotlinx.coroutines.flow.flow {
        for (user in users) {
            kotlinx.coroutines.delay(50) // شبیه‌سازی async
            emit(user)
        }
    }

    fun getUsersStartingWith(letter: Char) = getUsersFlow()
        .filter { it.name.startsWith(letter) }
}