package com.watb.chefmate.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.watb.chefmate.R
import com.watb.chefmate.data.PantryItem
import com.watb.chefmate.data.Recommendation
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.viewmodel.AppFlowViewModel
import com.watb.chefmate.viewmodel.RecipeViewModel
import com.watb.chefmate.viewmodel.UserViewModel
import java.time.LocalDate
import java.time.format.DateTimeParseException

private data class PantryStatusSummary(
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
    val safeCount: Int = 0
)

@SuppressLint("MemberExtensionConflict")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecipeClick: (Recipe) -> Unit,
    onOpenPantryTab: () -> Unit,
    navController: NavController,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel,
    appFlowViewModel: AppFlowViewModel
) {
    val scrollState = rememberScrollState()
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    val homeState by appFlowViewModel.homeState.collectAsState()
    var showRecommendationOverlay by remember { mutableStateOf(false) }

    val readyToCook = remember(homeState.readyToCook) {
        homeState.readyToCook.filter { it.recipeId > 0 }.distinctBy { it.recipeId }
    }
    val almostReady = remember(homeState.almostReady) {
        homeState.almostReady.filter { it.recipeId > 0 }.distinctBy { it.recipeId }
    }
    val pantryStatusSummary = remember(homeState.pantryItems) {
        buildPantryStatusSummary(homeState.pantryItems)
    }

    LaunchedEffect(isLoggedIn, user?.userId) {
        if (isLoggedIn && user != null) {
            appFlowViewModel.refreshHomeContext(user!!.userId)
            appFlowViewModel.prefetchChatHistorySources(user!!.userId)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF7))
    ) {
        Header(
            text = stringResource(R.string.home_title),
            trailingIcon = {
                IconButton(
                    onClick = { navController.navigate("homeSearch") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.home_search_open_content_description),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            BepesEntrySection(
                onOpenChat = {
                    if (isLoggedIn && user != null) {
                        navController.navigate("bepesChat?recipeId=-1")
                    } else {
                        navController.navigate("signIn")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            TodayEatCard(
                isLoggedIn = isLoggedIn,
                isLoading = homeState.isLoading || homeState.isRefreshingRecommendations,
                readyToCook = readyToCook,
                almostReady = almostReady,
                onClick = {
                    if (isLoggedIn && user != null) {
                        appFlowViewModel.refreshRecommendations(user!!.userId)
                        showRecommendationOverlay = true
                    } else {
                        navController.navigate("signIn")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            ExpiryAlertCard(
                isLoggedIn = isLoggedIn,
                summary = pantryStatusSummary,
                onOpenPantry = onOpenPantryTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showRecommendationOverlay && isLoggedIn && user != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showRecommendationOverlay = false },
            sheetState = sheetState,
            containerColor = Color(0xFFFFFBF5)
        ) {
            PantryRecommendationOverlay(
                isLoading = homeState.isLoading || homeState.isRefreshingRecommendations,
                readyToCook = readyToCook,
                almostReady = almostReady,
                onRefresh = { appFlowViewModel.refreshRecommendations(user!!.userId) },
                onOpenRecipeChat = { recipeId ->
                    showRecommendationOverlay = false
                    navController.navigate("bepesChat?recipeId=$recipeId")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun BepesEntrySection(
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.img_chatbot),
                    contentDescription = stringResource(R.string.home_bepes_title),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(100.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_bepes_title),
                        color = Color(0xFF9A3412),
                        fontSize = 19.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                    Text(
                        text = stringResource(R.string.home_bepes_description),
                        color = Color(0xFF7C2D12),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    )
                }
            }
            HomeActionChip(
                text = stringResource(R.string.home_chat_with_bepes),
                onClick = onOpenChat,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TodayEatCard(
    isLoggedIn: Boolean,
    isLoading: Boolean,
    readyToCook: List<Recommendation>,
    almostReady: List<Recommendation>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_today_eat_title),
                        color = Color(0xFF92400E),
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                    )
                    Text(
                        text = stringResource(R.string.home_today_eat_description),
                        color = Color(0xFF9A6B33),
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                StatusBadge(
                    text = when {
                        readyToCook.isNotEmpty() -> stringResource(R.string.home_ready_count, readyToCook.size)
                        almostReady.isNotEmpty() -> stringResource(R.string.home_almost_ready_count, almostReady.size)
                        else -> stringResource(R.string.home_ready_count, 0)
                    },
                    containerColor = Color(0xFFFFEDD5),
                    textColor = Color(0xFFC2410C)
                )
            }

            HomeActionChip(
                text = stringResource(R.string.home_open_pantry_ai),
                onClick = onClick,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                containerColor = Color(0xFFF97316),
                textColor = Color.White
            )
        }
    }
}

@Composable
private fun ExpiryAlertCard(
    isLoggedIn: Boolean,
    summary: PantryStatusSummary,
    onOpenPantry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.home_expiry_title),
                color = Color(0xFF0F172A),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Text(
                text = stringResource(R.string.home_expiry_description),
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 6.dp)
            )

            when {
                !isLoggedIn -> {
                    EmptyRecommendationCard(
                        text = stringResource(R.string.home_sign_in_for_expiry),
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }

                else -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    ) {

                        PantryStatusAction(
                            text = stringResource(R.string.home_pantry_status_expired, summary.expiredCount),
                            textColor = Color(0xFFDC2626),
                            onClick = onOpenPantry,
                            modifier = Modifier.weight(1f)
                        )
                        PantryStatusAction(
                            text = stringResource(R.string.home_pantry_status_expiring_soon, summary.expiringSoonCount),
                            textColor = Color(0xFFF97316),
                            onClick = onOpenPantry,
                            modifier = Modifier.weight(1f)
                        )
                        PantryStatusAction(
                            text = stringResource(R.string.home_pantry_status_safe, summary.safeCount),
                            textColor = Color(0xFF16A34A),
                            onClick = onOpenPantry,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PantryStatusAction(
    text: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                modifier = Modifier.weight(1f),
                lineHeight = 16.sp
            )
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 4.dp, top = 1.dp)
            )
        }
    }
}

@Composable
private fun PantryRecommendationOverlay(
    isLoading: Boolean,
    readyToCook: List<Recommendation>,
    almostReady: List<Recommendation>,
    onRefresh: () -> Unit,
    onOpenRecipeChat: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(bottom = 18.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.home_today_eat_title),
                color = Color(0xFF111827),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Text(
                text = stringResource(R.string.home_refresh),
                color = Color(0xFFF97316),
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                modifier = Modifier.clickable(onClick = onRefresh)
            )
        }

        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFFF97316))
            }
            return
        }

        Text(
            text = stringResource(R.string.home_ready_to_cook),
            color = Color(0xFF15803D),
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier.padding(top = 10.dp)
        )
        if (readyToCook.isEmpty()) {
            EmptyRecommendationCard(text = stringResource(R.string.home_empty_ready_to_cook))
        } else {
            readyToCook.take(6).forEach { recommendation ->
                RecommendationOverlayCard(
                    recommendation = recommendation,
                    onOpenRecipeChat = onOpenRecipeChat
                )
            }
        }

        Text(
            text = stringResource(R.string.home_almost_ready),
            color = Color(0xFFB45309),
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier.padding(top = 12.dp)
        )
        if (almostReady.isEmpty()) {
            EmptyRecommendationCard(text = stringResource(R.string.home_empty_almost_ready))
        } else {
            almostReady.take(6).forEach { recommendation ->
                RecommendationOverlayCard(
                    recommendation = recommendation,
                    onOpenRecipeChat = onOpenRecipeChat
                )
            }
        }
    }
}

@Composable
private fun RecommendationOverlayCard(
    recommendation: Recommendation,
    onOpenRecipeChat: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable { onOpenRecipeChat(recommendation.recipeId) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = recommendation.recipeName,
                color = Color(0xFF111827),
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
            Text(
                text = stringResource(R.string.home_completion_rate, recommendation.completionRate ?: 0),
                color = Color(0xFF6B7280),
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                modifier = Modifier.padding(top = 4.dp)
            )

            if (recommendation.missing.isNotEmpty()) {
                val missingText = recommendation.missing.joinToString { item ->
                    val need = item.need ?: 0.0
                    val unit = item.unit.orEmpty()
                    "${item.ingredientName} (${need}${unit})"
                }
                Text(
                    text = stringResource(R.string.home_missing, missingText),
                    color = Color(0xFF92400E),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            HomeActionChip(
                text = stringResource(R.string.home_cook_with_bepes),
                onClick = { onOpenRecipeChat(recommendation.recipeId) },
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyRecommendationCard(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            color = Color(0xFF6B7280),
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun StatusBadge(
    text: String,
    containerColor: Color,
    textColor: Color
) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun HomeActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFFEDD5),
    textColor: Color = Color(0xFFF97316)
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier.background(Color.Transparent)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            textAlign = TextAlign.Center
        )
    }
}

private fun buildPantryStatusSummary(items: List<PantryItem>): PantryStatusSummary {
    val today = LocalDate.now()
    var expiringSoonCount = 0
    var expiredCount = 0
    var safeCount = 0

    items.forEach { item ->
        val parsedDate = parsePantryDate(item.expiresAt) ?: return@forEach
        when {
            parsedDate.isBefore(today) -> expiredCount += 1
            !parsedDate.isAfter(today.plusDays(3)) -> expiringSoonCount += 1
            else -> safeCount += 1
        }
    }

    return PantryStatusSummary(
        expiringSoonCount = expiringSoonCount,
        expiredCount = expiredCount,
        safeCount = safeCount
    )
}

private fun parsePantryDate(value: String?): LocalDate? {
    if (value.isNullOrBlank()) return null
    val normalized = value.trim().take(10)
    return try {
        LocalDate.parse(normalized)
    } catch (_: DateTimeParseException) {
        null
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val appDatabase = AppDatabase.getDatabase(context)
    val userViewModel: UserViewModel = viewModel()
    val recipeRepository = RecipeRepository(appDatabase.recipeDao(), appDatabase.ingredientDao(), appDatabase.tagDao())
    val appFlowViewModel: AppFlowViewModel = viewModel()

    HomeScreen(
        onRecipeClick = {},
        onOpenPantryTab = {},
        navController = NavController(LocalContext.current),
        userViewModel = userViewModel,
        recipeViewModel = RecipeViewModel(recipeRepository),
        appFlowViewModel = appFlowViewModel
    )
}
