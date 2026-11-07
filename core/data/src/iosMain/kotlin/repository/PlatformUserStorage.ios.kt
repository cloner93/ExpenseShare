package repository

actual fun createPlatformUserStorage(): UserStorage = InMemoryUserStorage()
