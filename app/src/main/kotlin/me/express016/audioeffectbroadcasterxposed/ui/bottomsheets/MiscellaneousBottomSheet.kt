package me.express016.audioeffectbroadcasterxposed.ui.bottomsheets


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.express016.audioeffectbroadcasterxposed.R
import me.express016.audioeffectbroadcasterxposed.model.ExecutionMode
import me.express016.audioeffectbroadcasterxposed.ui.screens.home.HomeViewModel

val executionModeOptions = ExecutionMode.entries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiscellaneousSheetContent(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val initialDelay by viewModel.delay.collectAsStateWithLifecycle()
    val initialExecutionModeOption by viewModel.executionMode.collectAsStateWithLifecycle()

    var delay by remember(initialDelay) { mutableLongStateOf(initialDelay) }
    var executionModeOption by remember(initialExecutionModeOption) {
        mutableStateOf(
            initialExecutionModeOption
        )
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Miscellaneous", style = MaterialTheme.typography.titleLarge
        )


        HorizontalDivider()

        /*
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Top) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_info_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Execution Mode", style =
                            MaterialTheme.typography.titleMedium
                        )
                    }


                    HookInfoText(
                        label = "POST_HOOK",
                        description = "After the player has played/stopped music."
                    )
                    HookInfoText(
                        label = "PRE_HOOK",
                        description = "Before the player has played/stopped music."
                    )
                }
        }
         */

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(expanded, onExpandedChange = { expanded = !expanded }) {
            TextField(
                label = { Text("Execution Mode") },
                supportingText = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        HookInfoText(
                            label = "POST_HOOK",
                            description = "After the player has played/stopped music."
                        )
                        HookInfoText(
                            label = "PRE_HOOK",
                            description = "Before the player has played/stopped music."
                        )
                    }
                },

                value = executionModeOption.name,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        ImageVector.vectorResource(R.drawable.outline_terminal_24),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp)
            )


            ExposedDropdownMenu(
                expanded = expanded, onDismissRequest = { expanded = false }) {
                executionModeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            executionModeOption = option
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }

        }

        TextField(
            value = delay.toString(),
            onValueChange = { str ->
                if (str.all { it.isDigit() }) {
                    delay = try {
                        str.toLong()
                    } catch (_: NumberFormatException) {
                        0
                    }

                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Execute delay (ms)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.outline_timer_24),
                    contentDescription = null
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    viewModel.run {
                        saveDelay(delay)
                        saveExecutionMode(executionModeOption)
                    }
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.outline_save_24),
                        contentDescription = null
                    )
                    Text("Save")
                }
            }

            Button(
                onClick = {
                    delay = 0
                    executionModeOption = ExecutionMode.POST_HOOK
                    viewModel.run {
                        saveDelay(0)
                        saveExecutionMode(ExecutionMode.POST_HOOK)
                    }

                    Toast.makeText(context, "Reset", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.outline_refresh_24),
                        contentDescription = null
                    )
                    Text("Reset")
                }

            }
        }


    }
}

@Composable
private fun HookInfoText(label: String, description: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append("$label: ")
            }
            append(description)
        },
        style = MaterialTheme.typography.bodySmall
    )
}