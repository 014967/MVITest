package com.example.mvitest

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        val parseValue = Character.getNumericValue(input)
        if (parseValue in 0..9) {
            /*
            숫자일경우 계산식에 넣고, 결과값 계산
             */
            val postFixList = infixToPostFix(state.formula + input)
            val result = calculate(postFixList)
            reduce {
                state.copy(formula = state.formula + input, total = result)
            }
        } else {
            /*
            숫자를 누르기전에 연산자부터 누를경우 토스트 발생
             */
            if (state.formula.isEmpty()) {
                postSideEffect(CalculatorSideEffect.Toast("먼저 숫자를 눌러주세요"))
                return@intent
            }
            when (input) {
                'C' -> {
                    /*
                    계산식과 결과 모두 지운다.
                     */
                    reduce {
                        state.copy(total = 0, formula = "")
                    }
                }
                '=' -> {
                    /*
                    formula(계산식)으로 결과값 계산하기
                    중위 표현식을 후위표현식으로 바꾸고 결과값 계산
                     */
                    val postFixList = infixToPostFix(state.formula)
                    val result = calculate(postFixList)
                    reduce {
                        state.copy(total = result, formula = result.toString())
                    }
                }
                '<' -> {
                    if (state.formula.last() in listOf('-', '+', '*', '/', '%')) {
                            /*
                            연산자라면 지우기
                             */
                        val removedFormula = state.formula.dropLast(1)
                        reduce {
                            state.copy(formula = removedFormula)
                        }
                    } else {
                            /*
                            연산자가 아닌 숫자라면, 지우고 String이 비어있지 않다면, 결과값 다시 계산
                             */
                        val removedFormula = state.formula.dropLast(1)
                        if (removedFormula.isNotEmpty()) {
                            val postFixList = infixToPostFix(removedFormula)
                            val result = calculate(postFixList)
                            reduce {
                                state.copy(formula = removedFormula, total = result)
                            }
                        } else {
                            reduce {
                                state.copy(formula = "", total = 0)
                            }
                        }
                    }
                }
                else -> {
                    reduce {
                        /*
                        연산자를 더한다.
                         */
                        state.copy(formula = state.formula + input, total = 0)
                    }
                }
            }
        }
    }

    suspend fun infixToPostFix(formular: String): ArrayList<String> = withContext(Dispatchers.Default) {
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
        return@withContext postFixList
    }
}

suspend fun calculate(postFixList: ArrayList<String>): Int = withContext(Dispatchers.Default) {
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
        } else if (i == "<") {
        } else {
            stack.add(i.toInt())
        }
    }
    return@withContext stack.pop()
}

fun getPriority(operatorMap: HashMap<String, Int>, op1: String, op2: String): Boolean {
    val operator1 = operatorMap[op1]!!
    val operator2 = operatorMap[op2]!!

    return operator1 >= operator2
}
