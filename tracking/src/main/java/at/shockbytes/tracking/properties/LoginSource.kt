package at.shockbytes.tracking.properties

sealed class LoginSource(value: String) : BaseStringProperty(value) {

    override fun getKey(): String = "source"

    object FromMenu : LoginSource("menu")
    object FromSuggestion : LoginSource("suggest_book")
}
