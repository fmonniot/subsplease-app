package eu.monniot.subpleaseapp.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.monniot.subpleaseapp.BuildConfig
import eu.monniot.subpleaseapp.ui.theme.SubPleaseAppTheme
import java.text.SimpleDateFormat


@Composable
fun SettingsScreen() {

    val preferences = openSharedPrefs(LocalContext.current)

    LazyColumn(
        modifier = Modifier.padding(bottom = 56.dp) // for the bottom bar
    ) {
        item {
            Category(name = "Deluge") {
                TextPreference(
                    label = "URL",
                    dialogTitle = "Set the deluge url (including /json)",
                    pref = preferences.string("deluge_host"),
                )

                TextPreference(
                    label = "Username",
                    dialogTitle = "Set the deluge username",
                    pref = preferences.string("deluge_username"),
                )

                TextPreference(
                    label = "Password",
                    dialogTitle = "Set the deluge password",
                    pref = preferences.string("deluge_password"),
                )

                // TODO Doesn't work very well when toggled more than once for some reason
                val httpAuthPref = preferences.boolean("deluge_http_basic")
                val useHttpBasicAuth = remember { httpAuthPref.value() }

                SwitchPreference(
                    label = "HTTP Basic",
                    pref = httpAuthPref
                )

                TextPreference(
                    label = "HTTP user",
                    dialogTitle = "Set the deluge http user",
                    pref = preferences.string("deluge_http_username"),
                    disabled = !useHttpBasicAuth.value
                )

                TextPreference(
                    label = "HTTP password",
                    dialogTitle = "Set the deluge http password",
                    pref = preferences.string("deluge_http_password"),
                    disabled = !useHttpBasicAuth.value
                )
            }
        }

        item {
            Category(name = "Subsplease") {

                ClickPreference(
                    label = "Clear Cache",
                    summary = "Call subsplease on the next shows display",
                    onClick = { /*TODO*/ }
                )

                ListPreference(
                    label = "Download Quality",
                    pref = preferences.string("subsplease_download_quality"),
                    values = listOf("540p", "720p", "1080p")
                )
            }
        }

        item {
            Category(name = "About") {

                val date = java.util.Date(BuildConfig.BUILD_TIME.toLong())
                val buildDate = SimpleDateFormat.getDateInstance().format(date)

                ClickPreference(
                    label = "Version",
                    summary = "${BuildConfig.GIT_HASH} - $buildDate"
                )

                ClickPreference(label = "Licenses", summary = "TODO")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchPreference(
    label: String,
    pref: BooleanPreference
) {
    val value by pref.value()
    val v = value // immutable for smart cast

    ListItem(
        text = { Text(label) },
        trailing = {
            Switch(checked = v, onCheckedChange = { pref.set(it) })
        }
    )
}

@Composable
fun TextPreference(
    label: String,
    dialogTitle: String,
    pref: StringPreference,
    disabled: Boolean = false
) {
    val value by pref.value()
    val v = value // immutable for smart cast

    val displayed = if (v == null) "Not Set" else {
        val mask = '\u2022'.toString()

        mask.repeat(v.length)
    }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        TextFieldDialog(
            title = dialogTitle,
            defaultValue = v,
            onSave = { newText ->
                pref.set(newText)
                showDialog = false
            },
            onDismiss = { showDialog = false })
    }

    ClickPreference(label = label, summary = displayed, disabled) {
        if (!disabled) {
            showDialog = true
        }
    }
}

// Separate actual values from displayed values when adding multiple languages
@Composable
fun ListPreference(
    label: String,
    pref: StringPreference,
    values: List<String>
) {
    // Value
    val value by pref.value()
    val v = value ?: values.last() // immutable for smart cast

    // Dialog
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ListChoiceDialog(
            title = "Choose the show quality",
            values = values,
            defaultValue = v,
            onSave = { selectedOption ->
                pref.set(selectedOption)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }

    ClickPreference(label = label, summary = v) {
        showDialog = true
    }
}

@Composable
fun TextFieldDialog(
    title: String,
    defaultValue: String?,
    onSave: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(defaultValue) }

    SettingDialog(
        title,
        onSave = { onSave(selectedOption) },
        onDismiss = { onDismiss() }
    ) {
        TextField(
            value = selectedOption ?: "",
            onValueChange = { onOptionSelected(it) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListChoiceDialog(
    title: String,
    values: List<String>,
    defaultValue: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(defaultValue) }

    SettingDialog(
        title,
        onSave = {
            onSave(selectedOption)
        },
        onDismiss = { onDismiss() }
    ) {
        Column {
            values.forEach { option ->
                ListItem(
                    modifier = Modifier
                        .selectable(
                            selected = (option == selectedOption),
                            onClick = { onOptionSelected(option) }
                        ),
                    icon = {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = { onOptionSelected(option) }
                        )
                    },
                    text = { Text(option) }
                )
            }
        }
    }
}

@Composable
fun SettingDialog(
    title: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(title, style = MaterialTheme.typography.h6) },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(
                    modifier = Modifier.fillMaxHeight(),
                    onClick = {
                        onDismiss()
                    },
                    content = {
                        Text("Cancel", style = MaterialTheme.typography.button)
                    }
                )

                TextButton(
                    modifier = Modifier.fillMaxHeight(),
                    onClick = {
                        onSave()
                    },
                    content = {
                        Text("Save", style = MaterialTheme.typography.button)
                    }
                )
            }
        },
        text = {
            content()
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClickPreference(
    label: String,
    summary: String? = null,
    disabled: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val color =
        if (disabled) MaterialTheme.colors.onSurface.copy(alpha = .38f)
        else MaterialTheme.colors.onSurface

    ListItem(
        modifier = Modifier.clickable(enabled = !disabled) { onClick?.invoke() },
        text = {
            Text(label, color = color)
        },
        secondaryText = if (summary != null) {
            {
                Text(summary, color = color)
            }
        } else null
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Category(name: String, content: @Composable (() -> Unit)) {
    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
    Column(modifier = Modifier.padding(start = 56.dp, end = 16.dp)) {


        ListItem {
            Text(
                text = name,
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.primary
            )
        }

        content()
    }
}


@Preview
@Composable
fun SettingsPreview() {
    SubPleaseAppTheme {
        Surface {
            SettingsScreen()
        }
    }
}

// Doesn't work at the moment (can't render multiple window), which is inconvenient
// Because we don't have access to the dialog's surface outside of Dialog :(
@Preview
@Composable
fun ListChoicePreview() {
    SubPleaseAppTheme {
        Surface {
            ListChoiceDialog(
                "Pick one",
                listOf("Choice A", "Choice B", "Choice C", "Choice D"),
                "Choice A",
                {}, {}
            )
        }
    }
}
