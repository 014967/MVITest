package com.example.mvitest

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.withTimeout
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

/**
 * @Created by 김현국 2023/03/22
 */

class MainViewModel @Inject constructor() : ContainerHost<CalculatorState, CalculatorSideEffect>, ViewModel() {
    override val container = container<CalculatorState, CalculatorSideEffect>(CalculatorState())

    fun buttonClick(input: Char) = intent {
        postSideEffect(CalculatorSideEffect.Toast("Adding $input to ${state.total}"))
        val parseValue = Character.getNumericValue(input)
        if (parseValue in 0..9) {
            reduce {
                state.copy(total = state.total.doOperation(state.lastInput, parseValue))
            }
        } else {
            reduce {
                if (input == 'C') {
                    state.copy(total = 0, ' ')
                } else {
                    state.copy(lastInput = input)
                }
            }
        }
    }


}

fun Int.doOperation(operator: Char, x: Int) =
    when (operator) {
        '+' -> this + x
        '-' -> this - x
        '/' -> this / x
        '*' -> this * x
        ' ' -> x
        else -> 0
    }
