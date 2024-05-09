package com.example.calorifypro

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_EMAIL = "email"
        private const val KEY_SELECTED_DATE = "selectedDate"
    }

    var userEmail: String?
        get() = sharedPreferences.getString(KEY_EMAIL, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_EMAIL, value).apply()
        }

    var selectedDate: String?
        get() = sharedPreferences.getString(KEY_SELECTED_DATE, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_SELECTED_DATE, value).apply()
        }

    fun clearUserData() {
        sharedPreferences.edit().remove(KEY_EMAIL).remove(KEY_SELECTED_DATE).apply()
    }
}
