package com.example.mvitest

import dagger.Component
import javax.inject.Singleton

/**
 * @Created by 김현국 2023/03/22
 */

@Singleton
@Component
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(): AppComponent
    }
    fun inject(activity: MainActivity)
}
