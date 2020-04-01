package com.example.koattendance.data

import androidx.preference.PreferenceDataStore

class DataStore : PreferenceDataStore() {
    override fun putString(key: String, value: String?) {
        // Save the value somewhere
    }

    override fun getString(key: String, defValue: String?): String? {
        // Retrieve the value

        return  getString(key, defValue)
    }
}