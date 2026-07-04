package com.cncindex

import android.app.Application
import com.cncindex.data.local.database.AppDatabase
import com.cncindex.data.repository.CncRepository

class CncIndexApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy {
        CncRepository(
            context = this,
            programDao = database.programDao(),
            toolDao = database.toolDao()
        )
    }
}
