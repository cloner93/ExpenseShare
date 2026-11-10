package org.milad.expense_share.network.wasm

external interface JsStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
    fun clear()
}

external val localStorage: JsStorage

external interface JsLocation {
    val hostname: String
    val origin: String
}

external interface JsWindow {
    val location: JsLocation
}

external val window: JsWindow
