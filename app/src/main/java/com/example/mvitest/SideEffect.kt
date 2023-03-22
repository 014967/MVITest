package com.example.mvitest

/**
 * @Created by 김현국 2023/03/22
 */

sealed class CalculatorSideEffect {
    data class Toast(val text: String) : CalculatorSideEffect()
}
