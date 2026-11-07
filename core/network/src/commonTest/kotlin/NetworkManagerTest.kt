import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import plugin.UnauthorizedException

class NetworkManagerTest : DescribeSpec({
    describe("Safe Network Call") {
        context("when request is successful") {
            it("should emit success when response.success = true") {
                val result = safeNetworkCall<String> {
                    SuccessResponse(true, "Hello")
                }.first()

                result.shouldBeSuccess { value ->
                    value shouldBe "Hello"
                }
            }
        }
        context("when request is fail") {
            it("should emit failure when response.success = false") {
                val result = safeNetworkCall<String> {
                    SuccessResponse(false, "Hello")
                }.first()

                result.shouldBeFailure { err ->
                    err.message shouldBe "Request failed"
                }
            }
            it("should emit failure when block throws exception") {
                val result = safeNetworkCall<String> {
                    throw UnauthorizedException()
                }.first()

                result.shouldBeFailure { err ->
                    err.message shouldBe "Authentication failed"
                }
            }
        }
    }
})