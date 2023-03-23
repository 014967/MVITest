package com.example.mvitest

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.util.Stack
import javax.inject.Inject

/**
 * @Created by 김현국 2023/03/22
 */

class MainViewModel @Inject constructor() : ContainerHost<CalculatorState, CalculatorSideEffect>, ViewModel() {
    override val container = container<CalculatorState, CalculatorSideEffect>(CalculatorState())

    private val operatorMap = HashMap<String, Int>().apply {
        put("*", 3)
        put("/", 3)
        put("%", 3)
        put("+", 2)
        put("-", 2)
    }

    fun buttonClick(input: Char) = intent {
        postSideEffect(CalculatorSideEffect.Toast("Adding $input to ${state.total}"))
        val parseValue = Character.getNumericValue(input)
        if (parseValue in 0..9) {
            reduce {
                state.copy(formula = state.formula + input)
            }
        } else {
            reduce {
                when (input) {
                    'C' -> {
                        state.copy(total = 0, lastInput = ' ', formula = "")
                    }
                    '=' -> {
                        val postFixList = infixToPostFix(state.formula)
                        val result = calculate(postFixList)
                        state.copy(total = result, formula = result.toString())
                    }
                    else -> {
                        state.copy(formula = state.formula + input, total = 0)
                    }
                }
            }
        }
    }

    fun infixToPostFix(formular: String): ArrayList<String> {
        val newFormular = StringBuilder()
        for (i in formular) {
            if (i in listOf('-', '+', '*', '/', '%')) {
                newFormular.append(" $i ")
            } else {
                newFormular.append(i)
            }
        }

        val stack = Stack<String>()
        val postFixList = arrayListOf<String>()
        for (i in newFormular.split(" ")) {
            if (i in listOf("-", "+", "*", "/", "%")) {
                while (stack.isNotEmpty() && getPriority(operatorMap, stack.peek().toString(), i)) {
                    postFixList.add(stack.pop().toString())
                }
                stack.push(i)
            } else {
                postFixList.add(i)
            }
        }

        while (stack.isNotEmpty()) {
            postFixList.add(stack.pop())
        }
        return postFixList
    }
}

fun calculate(postFixList: ArrayList<String>): Int {
    val stack = Stack<Int>()
    for (i in postFixList) {
        if (i in listOf("*", "-", "+", "/", "%")) {
            val a = stack.pop()
            val b = stack.pop()
            when (i) {
                "*" -> {
                    stack.add(a * b)
                }
                "-" -> {
                    stack.add(b - a)
                }
                "+" -> {
                    stack.add(a + b)
                }
                "/" -> {
                    stack.add(b / a)
                }
                "%" -> {
                    stack.add(b % a)
                }
            }
        } else {
            stack.add(i.toInt())
        }
    }
    return stack.pop()
}

fun getPriority(operatorMap: HashMap<String, Int>, op1: String, op2: String): Boolean {
    val operator1 = operatorMap[op1]!!
    val operator2 = operatorMap[op2]!!

    return operator1 >= operator2
}
