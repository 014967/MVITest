package com.example.mvitest

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mvitest.ui.theme.MVITestTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.consumeAsFlow
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MviApplication).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        setContent {
            MVITestTheme {
                val state by mainViewModel.state.collectAsState()

                val context = LocalContext.current

                val sideEffect = mainViewModel.sideEffect.consumeAsFlow()
                LaunchedEffect(true) {
                    sideEffect.collect { sideEffect ->
                        handleSideEffect(context, sideEffect)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                    ) {
                        FormularText(formular = state.formular, fontSize = 75.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        ResultText(result = state.result.toString(), fontSize = 50.sp)
                    }

                    Calculator(config = calCulatorConfig.toImmutableList(), onButtonClick = mainViewModel::buttonClick)
                }
            }
        }
    }
}

private fun handleSideEffect(context: Context, sideEffect: CalculateSideEffect) {
    when (sideEffect) {
        is CalculateSideEffect.ShowToast -> Toast.makeText(context, sideEffect.text, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun FormularText(formular: String, fontSize: TextUnit) {
    Text(
        text = formular,
        fontSize = fontSize,
    )
}

@Composable
fun ResultText(result: String, fontSize: TextUnit) {
    Text(
        text = if (result == "0") "" else result,
        fontSize = fontSize,
    )
}

@Composable
fun Calculator(
    modifier: Modifier = Modifier,
    config: ImmutableList<Char>,
    onButtonClick: (Char) -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(4),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        items(config) {
            if (it != ' ') {
                CircleButton(it, onButtonClick)
            }
        }
    }
}

@Composable
fun CircleButton(config: Char, onButtonClick: (Char) -> Unit) {
    Row(
        modifier = Modifier.size(100.dp).clip(
            CircleShape,
        ).clickable {
            onButtonClick(config)
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = config.toString(),
            fontSize = 50.sp,
        )
    }
}

@Preview
@Composable
fun PreviewCalCulator() {
    MaterialTheme() {
        Calculator(
            modifier = Modifier.fillMaxSize(),
            config = calCulatorConfig.toImmutableList(),
        ) {}
//
    }
}
val calCulatorConfig = listOf(
    'C', '<', '%', '/',
    '7', '8', '9', '*',
    '4', '5', '6', '-',
    '1', '2', '3', '+',
    ' ', '0', ' ', '=',
)
