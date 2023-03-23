package com.example.mvitest

import android.app.Application

/**
 * @Created by 김현국 2023/03/22
 */
class MviApplication : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create()
    }
}
