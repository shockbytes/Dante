package at.shockbytes.dante.util.explanations

import android.content.SharedPreferences

class SharedPrefsExplanations(
    private val sharedPreferences: SharedPreferences
) : Explanations {

    override fun suggestion(): Explanation.Suggestion {
        return Explanation.Suggestion(
            show = getShowFor<Explanation.Suggestion>(),
            userWantsToSuggest = getBooleanForKey(SUGGESTION_USER_WANTS_TO_SUGGEST, false)
        )
    }

    private fun getBooleanForKey(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun wishlist(): Explanation.Wishlist {
        return Explanation.Wishlist(show = getShowFor<Explanation.Wishlist>())
    }

    private inline fun <reified T : Explanation> getShowFor(): Boolean {
        return sharedPreferences.getBoolean(T::class.java.simpleName, true)
    }

    override fun markSeen(explanation: Explanation) {
        putBoolean(explanation::class.java.simpleName, false)
    }

    override fun update(explanation: Explanation) {
        when (explanation) {
            is Explanation.Suggestion -> {
                putBoolean(SUGGESTION_USER_WANTS_TO_SUGGEST, explanation.userWantsToSuggest)
            }
            else -> Unit
        }
    }

    private fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    companion object {
        private const val SUGGESTION_USER_WANTS_TO_SUGGEST = "user_wants_to_suggest"
    }
}