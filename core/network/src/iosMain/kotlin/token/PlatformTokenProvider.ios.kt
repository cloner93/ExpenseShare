package token

actual fun createPlatformTokenProvider(): TokenProvider = InMemoryTokenProvider()
