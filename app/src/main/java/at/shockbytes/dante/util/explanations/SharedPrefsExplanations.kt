package at.shockbytes.dante.util.explanations

import android.content.SharedPreferences

class SharedPrefsExplanations(
    private val sharedPreferences: SharedPreferences
) : Explanations {

    override fun suggestion(): Explanation.Suggestion {
        return Explanation.Suggestion(
            show = getShowFor<Explanation.Suggestion>(),
            userWantsToSuggest = getBooleanForKey("user_wants_to_suggest", false)
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
        sharedPreferences.edit().putBoolean(explanation::class.java.simpleName, true).apply()
    }

}