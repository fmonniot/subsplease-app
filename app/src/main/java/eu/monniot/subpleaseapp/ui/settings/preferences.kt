package eu.monniot.subpleaseapp.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


private val DEVICE_TOKEN_FIELD = "deviceToken"
private val USER_TOKEN_FIELD = "userToken"

internal fun openSharedPrefs(context: Context): SharedPreferences {
    val mainKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        "encrypted",
        mainKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

fun SharedPreferences.string(key: String): StringPreference {
    return StringPreference(this, key)
}

fun SharedPreferences.boolean(key: String): BooleanPreference {
    return BooleanPreference(this, key)
}

open class Pref<T>(
    private val pref: SharedPreferences,
    getV: () -> T,
    private val setV: SharedPreferences.Editor.(T) -> Unit
) {
    private val state = mutableStateOf(getV())

    fun value(): State<T> {
        return state
    }

    fun set(s: T) {
        with(pref.edit()) {
            setV(s)

            apply()
        }
        state.value = s
    }
}

class StringPreference(pref: SharedPreferences, key: String) :
    Pref<String?>(pref, { pref.getString(key, null) }, { v -> putString(key, v) })

class BooleanPreference(pref: SharedPreferences, key: String) :
    Pref<Boolean>(pref, { pref.getBoolean(key, false) }, { b -> putBoolean(key, b) })
