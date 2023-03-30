package com.example.mvitest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvitest.mvi.Intent
import com.example.mvitest.mvi.Model
import com.example.mvitest.mvi.SideEffect
import com.example.mvitest.mvi.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Stack
import javax.inject.Inject

/**
 * @Created by 김현국 2023/03/22
 */
sealed class CalculateIntent : Intent {
    object Remove : CalculateIntent()
    object Refresh : CalculateIntent()
    object Calculate : CalculateIntent()
    data class AddNumber(val number: Int) : CalculateIntent()
    data class AddOperator(val operator: Char) : CalculateIntent()
}

sealed class CalculateSideEffect : SideEffect {
    data class ShowToast(val text: String) : CalculateSideEffect()
}
data class CalculateState(
    val formular: String = "",
    val result: Int = 0,
) : State
class MainViewModel @Inject constructor() : Model<CalculateState, CalculateSideEffect, CalculateIntent>, ViewModel() {

    override val intents: Channel<CalculateIntent> = Channel(Channel.UNLIMITED)
    override val sideEffect: Channel<CalculateSideEffect> = Channel(Channel.UNLIMITED)

    private val _state: MutableStateFlow<CalculateState> = MutableStateFlow(CalculateState())
    override val state: StateFlow<CalculateState> = _state.asStateFlow()

    private val operatorMap = HashMap<String, Int>().apply {
        put("*", 3)
        put("/", 3)
        put("%", 3)
        put("+", 2)
        put("-", 2)
    }

    init {
        intentConsumer()
    }
    private fun intentConsumer() {
        viewModelScope.launch {
            intents.consumeAsFlow().collect { calculateIntent: CalculateIntent ->
                when (calculateIntent) {
                    is CalculateIntent.Refresh -> {
                        refreshState()
                    }
                    is CalculateIntent.Remove -> {
                        removeFormular()
                    }
                    is CalculateIntent.Calculate -> {
                        emitCalculateResult()
                    }
                    is CalculateIntent.AddNumber -> {
                        numberButtonClick(calculateIntent.number)
                    }
                    is CalculateIntent.AddOperator -> {
                        operatorButtonClick(calculateIntent.operator)
                    }
                }
            }
        }
    }

    fun buttonClick(input: Char) {
        viewModelScope.launch {
            val parseInput = Character.getNumericValue(input)
            if (parseInput in 0..9) {
                intents.send(CalculateIntent.AddNumber(number = parseInput))
            } else {
                if (_state.value.formular.isEmpty()) {
                    sideEffect.send(CalculateSideEffect.ShowToast("먼저 숫자를 눌러라"))
                    return@launch
                }
                when (input) {
                    'C' -> {
                        intents.send(CalculateIntent.Refresh)
                    }
                    '=' -> {
                        intents.send(CalculateIntent.Calculate)
                    }
                    '<' -> {
                        intents.send(CalculateIntent.Remove)
                    }
                    '+', '-', '/', '*', '%' -> {
                        intents.send(CalculateIntent.AddOperator(operator = input))
                    }
                }
            }
        }
    }

    private suspend fun numberButtonClick(number: Int) = with(_state.value) {
        if (formular.length == 1 && formular == "0" && number == 0) {
            return
        } else if (formular.length == 1 && number != 0) {
            _state.value = copy(formular = number.toString(), result = number)
        } else {
            val postFixList = infixToPostFix(formular + number)
            val result = calculate(postFixList)
            _state.value = copy(formular = formular + number, result = result)
        }
    }

    private fun operatorButtonClick(operator: Char) = with(_state.value) {
        _state.value = copy(formular = formular + operator, result = 0)
    }

    private fun refreshState() = with(_state.value) {
        _state.value = copy(formular = "", result = 0)
    }

    private suspend fun emitCalculateResult() = with(_state.value) {
        val postFixList = infixToPostFix(formular)
        val result = calculate(postFixList)
        _state.value = copy(formular = result.toString(), result = result)
    }
    private suspend fun removeFormular() = with(_state.value) {
        if (_state.value.formular.isEmpty()) {
            return
        }
        if (formular.last() in listOf('-', '+', '*', '/', '%')) {
            /*
            연산자라면 지우기
             */
            _state.value = copy(formular = formular.dropLast(1))
        } else {
            /*
            연산자가 아닌 숫자라면, 지우고 String이 비어있지 않다면, 결과값 다시 계산
             */
            val removedFormula = formular.dropLast(1)
            if (removedFormula.isNotEmpty()) {
                val postFixList = infixToPostFix(removedFormula)
                val result = calculate(postFixList)
                _state.value = copy(formular = removedFormula, result = result)
            } else {
                _state.value = copy(formular = "", result = 0)
            }
        }
    }

    private suspend fun infixToPostFix(formular: String): ArrayList<String> = withContext(Dispatchers.Default) {
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
    private suspend fun calculate(postFixList: ArrayList<String>): Int = withContext(Dispatchers.Default) {
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

    private fun getPriority(operatorMap: HashMap<String, Int>, op1: String, op2: String): Boolean {
        val operator1 = operatorMap[op1]!!
        val operator2 = operatorMap[op2]!!

        return operator1 >= operator2
    }
}
