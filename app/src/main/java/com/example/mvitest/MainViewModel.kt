package com.example.mvitest

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Stack
import javax.inject.Inject

/**
 * @Created by 김현국 2023/03/22
 */

class MainViewModel @Inject constructor() : ViewModel() {
    private val _container: MutableStateFlow<CalculatorState> = MutableStateFlow(CalculatorState())
    val container = _container.asStateFlow()

    private val operatorMap = HashMap<String, Int>().apply {
        put("*", 3)
        put("/", 3)
        put("%", 3)
        put("+", 2)
        put("-", 2)
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
