package org.milad.expense_share

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform