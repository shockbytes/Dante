package at.shockbytes.dante.util.explanations

import android.content.SharedPreferences

class SharedPrefsExplanations(
    private val sharedPreferences: SharedPreferences
) : Explanations {

    override fun suggestion(): Explanation.Suggestion {
        return Explanation.Suggestion(
            show = getShowFor<Explanation.Suggestion>()
        )
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
        // Not used now...
    }

    private fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
}