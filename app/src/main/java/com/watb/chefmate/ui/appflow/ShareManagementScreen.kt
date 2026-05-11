package com.watb.chefmate.ui.appflow

import android.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.PantryShare
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.viewmodel.AppFlowViewModel
import com.watb.chefmate.viewmodel.UserViewModel

@Composable
fun ShareManagementScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    appFlowViewModel: AppFlowViewModel,
    pantryId: Int
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val user by userViewModel.user.collectAsState()
    val homeState by appFlowViewModel.homeState.collectAsState()
    var shares by remember { mutableStateOf<List<PantryShare>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedRoleExpanded by remember { mutableStateOf<Int?>(null) }

    val removeTitle = stringResource(R.string.share_remove_title)
    val removeMessage = stringResource(R.string.share_remove_message, "")
    val removeConfirmText = stringResource(R.string.common_remove)
    val cancelText = stringResource(R.string.common_cancel)

    LaunchedEffect(pantryId, user?.userId) {
        val userId = user?.userId ?: return@LaunchedEffect
        loading = true
        appFlowViewModel.loadPantryShares(userId, pantryId)
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Header(
            text = stringResource(R.string.share_management_title),
            leadingIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.common_back_content_description),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.common_loading),
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )
            }
        } else if (shares.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.share_no_users),
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(shares, key = { it.userId }) { share ->
                    ShareUserCard(
                        share = share,
                        isOwner = share.role == "owner",
                        expanded = selectedRoleExpanded == share.userId,
                        onExpandToggle = {
                            selectedRoleExpanded = if (selectedRoleExpanded == share.userId) null else share.userId
                        },
                        onRoleChange = { newRole ->
                            selectedRoleExpanded = null
                            user?.userId?.let { userId ->
                                appFlowViewModel.updateShareRole(userId, pantryId, share.userId, newRole)
                            }
                        },
                        onRemove = {
                            AlertDialog.Builder(context)
                                .setTitle(removeTitle)
                                .setMessage(removeMessage.replace("%1\$s", share.fullName))
                                .setPositiveButton(removeConfirmText) { _, _ ->
                                    user?.userId?.let { userId ->
                                        appFlowViewModel.removeShare(userId, pantryId, share.userId)
                                        shares = shares.filterNot { it.userId == share.userId }
                                    }
                                }
                                .setNegativeButton(cancelText, null)
                                .show()
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }
}

@Composable
private fun ShareUserCard(
    share: PantryShare,
    isOwner: Boolean,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onRoleChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = share.fullName,
                    color = Color(0xFF111827),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    modifier = Modifier.weight(1f)
                )
                RoleBadge(role = share.role)
            }

            if (share.sharedAt.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.share_shared_at, share.sharedAt),
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!isOwner) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box {
                        Text(
                            text = share.role.replaceFirstChar { it.uppercase() },
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .clickable(onClick = onExpandToggle)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { onExpandToggle() }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.share_role_editor)) },
                                onClick = { onRoleChange("editor") }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.share_role_viewer)) },
                                onClick = { onRoleChange("viewer") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.common_remove),
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                        modifier = Modifier
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                            .clickable(onClick = onRemove)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val (bgColor, textColor) = when (role.lowercase()) {
        "owner" -> Color(0xFFDCFCE7) to Color(0xFF166534)
        "editor" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        "viewer" -> Color(0xFFF1F5F9) to Color(0xFF475569)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }
    Text(
        text = role.replaceFirstChar { it.uppercase() },
        color = textColor,
        fontSize = 11.sp,
        fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}