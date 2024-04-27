package com.example.androidgeminiaichatbot.Utils

import android.net.Uri
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope

class UriCustomSaver : Saver<MutableList<Uri>, List<String>> {
    override fun restore(value: List<String>): MutableList<Uri>? {
        return value.map { Uri.parse(it) }.toMutableList()
    }

    override fun SaverScope.save(value: MutableList<Uri>): List<String>? {
        return value.map { it.toString() }
    }
}