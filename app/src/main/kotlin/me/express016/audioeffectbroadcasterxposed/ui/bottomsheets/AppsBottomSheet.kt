package me.express016.audioeffectbroadcasterxposed.ui.bottomsheets


import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.express016.audioeffectbroadcasterxposed.R
import me.express016.audioeffectbroadcasterxposed.ui.screens.home.HomeViewModel

data class AppUiModel(
    val packageName: String, val label: String, val icon: Any
)

object AppListCache {
    val appList = mutableStateListOf<AppUiModel>()
    var isLoading = mutableStateOf(true)
}

@Composable
fun AppsSheetContent(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val pm = remember { context.packageManager }

    val checkedApps by viewModel.checkedApps.collectAsStateWithLifecycle()
    var appList = AppListCache.appList

    var searchQuery by remember { mutableStateOf("") }

    if (appList.isEmpty()) {
        LaunchedEffect(Unit) {

            val apps = withContext(Dispatchers.IO) {
                pm.getInstalledApplications(0)
                    .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && it.enabled && it.packageName != context.packageName }

                    .map { info ->
                        AppUiModel(
                            packageName = info.packageName,
                            label = pm.getApplicationLabel(info).toString(),
                            icon = pm.getApplicationIcon(info)
                        )
                    }
                    .sortedWith(compareByDescending<AppUiModel> { checkedApps.contains(it.packageName) }.thenBy { it.label.lowercase() })
            }

            appList.addAll(apps)

            AppListCache.isLoading.value = false
        }

    } else {
        appList =
            appList.sortedWith(compareByDescending<AppUiModel> { checkedApps.contains(it.packageName) }.thenBy { it.label.lowercase() })
                .toMutableStateList()
    }

    val filteredList = remember(searchQuery, appList.size) {
        if (searchQuery.isBlank()) appList
        else appList.filter {
            it.label.contains(
                searchQuery,
                ignoreCase = true
            ) || it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Forced AudioTrack Apps", style = MaterialTheme.typography.titleLarge
        )


        HorizontalDivider()

        if (AppListCache.isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (appList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No apps found", style = MaterialTheme.typography.bodyLarge
                )
            }

        } else {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps...") },
                leadingIcon = {
                    Icon(
                        ImageVector.vectorResource(R.drawable.outline_search_24),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                        }
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.outline_close_24),
                            contentDescription = null,
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn {
                itemsIndexed(
                    items = filteredList, key = { _, app -> app.packageName }) { index, app ->
                    val shape = when {
                        filteredList.size == 1 -> RoundedCornerShape(16.dp)
                        index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        index == filteredList.size - 1 -> RoundedCornerShape(
                            bottomStart = 16.dp, bottomEnd = 16.dp
                        )

                        else -> RectangleShape
                    }

                    AppRowItem(
                        app = app,
                        isChecked = checkedApps.contains(app.packageName),
                        shape = shape,
                        onToggle = { viewModel.toggleApp(app.packageName) })

                    if (index < filteredList.size - 1) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRowItem(
    app: AppUiModel, isChecked: Boolean, shape: Shape, onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = app.icon, contentDescription = null, modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(app.label, style = MaterialTheme.typography.bodyLarge)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }

            Checkbox(
                checked = isChecked, onCheckedChange = { onToggle() })
        }
    }
}