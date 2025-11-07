import client.ApiClient
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockative.mock
import io.mockative.of
import kotlinx.coroutines.flow.first
import plugin.UnauthorizedException

class NetworkManagerTest : DescribeSpec({
    val client = mock(of<ApiClient>())
    val networkManager: NetworkManager = NetworkManager(client)

    describe("Safe Network Call") {
        context("when request is successful") {
            it("should emit success when response.success = true") {
                val flow = networkManager.safeNetworkCall {
                    SuccessResponse(true, "Hello")
                }.first()

                flow.shouldBeSuccess() {
                    it.shouldBe("Hello")
                }
            }
        }
        context("when request is fail") {
            it("should emit failure when response.success = false") {
                val flow = networkManager.safeNetworkCall {
                    SuccessResponse(false, "Hello")
                }.first()

                flow.shouldBeFailure() {
                    it.message shouldBe "Request failed"
                }
            }
            it("should emit failure when block throws exception") {
                val flow = networkManager.safeNetworkCall<String> {
                    throw UnauthorizedException()
                }.first()

                flow.shouldBeFailure() {
                    it.message shouldBe "Authentication failed"
                }
            }
        }
    }
})