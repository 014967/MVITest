package com.example.mvitest.mvi

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

/**
 * @Created by 김현국 2023/03/30
 */
interface Model<STATE : State, SIDE_EFFECT : SideEffect, INTENT : Intent> {
    val intents: Channel<INTENT>
    val state: StateFlow<STATE>
    val sideEffect: Channel<SIDE_EFFECT>
}
