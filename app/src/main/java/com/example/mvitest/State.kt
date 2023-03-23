package com.example.mvitest

/**
 * @Created by 김현국 2023/03/22
 */

data class CalculatorState(
    val formula: String = "",
    val total: Int = 0,
    val lastInput: Char = ' '
)
