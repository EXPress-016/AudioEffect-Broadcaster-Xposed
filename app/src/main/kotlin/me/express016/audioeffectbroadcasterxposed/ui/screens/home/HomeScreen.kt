package me.express016.audioeffectbroadcasterxposed.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.express016.audioeffectbroadcasterxposed.BuildConfig
import me.express016.audioeffectbroadcasterxposed.R
import me.express016.audioeffectbroadcasterxposed.model.SheetType
import me.express016.audioeffectbroadcasterxposed.ui.bottomsheets.AppsSheetContent
import me.express016.audioeffectbroadcasterxposed.ui.bottomsheets.MiscellaneousSheetContent

sealed class IconResource {
    data class Vector(val imageVector: ImageVector) : IconResource()
    data class ResID(val resId: Int) : IconResource()
}

data class MenuItem(
    val id: String,
    val label: String,
    val icon: IconResource,
)

val menuItems = listOf(
    MenuItem(
        "forced_audiotrack_apps",
        "Manage Forced AudioTrack Apps",
        IconResource.ResID(R.drawable.outline_android_24)
    ), MenuItem("misc", "Miscellaneous", IconResource.ResID(R.drawable.outline_settings_24))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel, isModuleActive: () -> Boolean
) {

    LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true, confirmValueChange = {
            if (it == SheetValue.Hidden) {
                viewModel.dismissSheet()
            }
            true
        })

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("Audio Effect Broadcaster", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
                StatusCard(isModuleActive)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
            }

            items(items = menuItems, key = { it.id }) { item ->
                MenuListItem(
                    data = item,
                    enabled = isModuleActive(),
                    onClick = { viewModel.onMenuItemClicked(item.id) })
            }
        }

        if (viewModel.currentSheet != SheetType.NONE) {

            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissSheet() },
                sheetState = sheetState,
                dragHandle = {
                    BottomSheetDefaults.DragHandle(width = 64.dp)
                }
            ) {
                SheetContent(viewModel.currentSheet, viewModel)
            }
        }
    }
}

@Composable
private fun SheetContent(type: SheetType, viewModel: HomeViewModel) {
    when (type) {
        SheetType.FORCED_AUDIOTRACK_APPS -> AppsSheetContent(
            viewModel = viewModel
        )

        SheetType.MISCELLANEOUS -> MiscellaneousSheetContent(viewModel = viewModel)
        else -> Unit
    }
}


@Composable
private fun StatusCard(
    isModuleActive: () -> Boolean
) {
    val enabled = isModuleActive()
    val statusText = if (enabled) "Module Activated" else "Module Not Activated"
    val statusIcon =
        if (enabled) ImageVector.vectorResource(R.drawable.outline_check_24) else ImageVector.vectorResource(
            R.drawable.outline_close_24
        )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
            disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        enabled = enabled, onClick = {},
    ) {
        Row(
            modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "$statusText [${BuildConfig.VERSION_CODE}]",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}


@Composable
private fun MenuListItem(
    modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit, data: MenuItem
) {
    Surface(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (enabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceDim,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val icon = when (data.icon) {
                is IconResource.Vector -> data.icon.imageVector
                is IconResource.ResID -> ImageVector.vectorResource(id = data.icon.resId)
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = data.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

