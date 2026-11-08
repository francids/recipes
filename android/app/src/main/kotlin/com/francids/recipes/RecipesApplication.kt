package com.francids.recipes

import android.app.Application
import com.francids.recipes.data.Messages

class RecipesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Messages.load(this)
    }
}
