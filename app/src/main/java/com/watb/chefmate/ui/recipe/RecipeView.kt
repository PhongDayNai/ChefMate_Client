package com.watb.chefmate.ui.recipe

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiClient
import com.watb.chefmate.api.ApiConstant
import com.watb.chefmate.ui.theme.Header
import com.watb.chefmate.data.CommentItem
import com.watb.chefmate.data.CookingStep
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.database.entities.TagEntity
import com.watb.chefmate.helper.CommonHelper
import com.watb.chefmate.helper.DataStoreHelper
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.viewmodel.RecipeViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import kotlin.text.isNotBlank

@SuppressLint("MemberExtensionConflict")
@OptIn(FlowPreview::class)
@Composable
fun RecipeViewScreen(
    navController: NavController,
    recipe: Recipe,
    isHistory: Boolean = false,
    recipeViewModel: RecipeViewModel
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val recipeState by recipeViewModel.getRecipeByName(recipe.recipeName).collectAsState(initial = null)

    var isLoggedIn by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf<Int?>(null) }
    var markPainter by remember { mutableStateOf(R.drawable.ic_mark) }
    var isLiked by remember { mutableStateOf(recipe.isLiked) }
    var likeQuantity by remember { mutableStateOf(recipe.likeQuantity) }
    var commentQuantity by remember { mutableStateOf(recipe.comments.size) }
    var viewQuantity by remember { mutableStateOf(recipe.viewCount) }
    val comments = remember { recipe.comments.toMutableStateList() }
    var isLoading by remember { mutableStateOf(false) }

    var selectedPageManual by remember { mutableIntStateOf(-1) }

    val selectedPage by remember {
        derivedStateOf {
            if (selectedPageManual != -1) {
                selectedPageManual
            } else {
                if (lazyListState.firstVisibleItemIndex == 0) 0
                else lazyListState.firstVisibleItemIndex - 1
            }
        }
    }

    val isExpanded by remember(lazyListState) {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }

    val animateFooter by animateFloatAsState(
        targetValue = if (selectedPage == 0) 0.25f else 0.75f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "Footer Offset"
    )

    LaunchedEffect(Unit) {
        launch {
            if (!isHistory) {
                recipe.recipeId?.let {
                    val response = ApiClient.increaseViewCount(recipe.recipeId)
                    if (response != null) {
                        if (response.success) {
                            if (response.data != null) {
                                viewQuantity = response.data.count
                            } else {
                                Log.e("RecipeViewScreen", "Error: ${response.message}")
                            }
                        }
                    }
                }
            }
        }
        launch {
            isLoggedIn = DataStoreHelper.isLoggedIn(context)
            userId = DataStoreHelper.getUserId(context)
        }
    }

    LaunchedEffect(recipeState) {
        Log.d("RecipeViewScreen", "recipeName: ${recipe.recipeName}")
        Log.d("RecipeViewScreen", "recipeState: $recipeState")
        if (recipeState != null) {
            markPainter = R.drawable.ic_mark_filled
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .debounce(100)
            .collect {
                selectedPageManual = if (it == 0) 0
                else it - 1
            }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
    ) {
        Header(
            leadingIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            },
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = {
                            if (markPainter == R.drawable.ic_mark) {
                                if (!isHistory) {
                                    if (recipe.image.startsWith("/")) {
                                        isLoading = true
                                        ApiClient.downloadAndSaveImage(
                                            context = context,
                                            coroutineScope = coroutineScope,
                                            imageUrl = "${ApiConstant.MAIN_URL}${recipe.image}",
                                            onResult = { status ->
                                                if (status.startsWith("Success")) {
                                                    val ingredientsToSave = recipe.ingredients.filter { it.ingredientName.isNotBlank() }.map {
                                                        Pair(it.ingredientName, Pair(it.weight, it.unit))
                                                    }
                                                    val stepsToSave = recipe.cookingSteps.filter { it.stepContent.isNotBlank() }.map {
                                                        Pair(it.indexStep ?: 0, it.stepContent)
                                                    }
                                                    val tags = recipe.tags.map { tag ->
                                                        tag.tagName
                                                    }

                                                    coroutineScope.launch {
                                                        recipeViewModel.addRecipe(
                                                            recipeName = recipe.recipeName,
                                                            imageUri = status.substringAfter("Success: "),
                                                            userName = recipe.userName,
                                                            isPublic = false,
                                                            likeQuantity = recipe.likeQuantity,
                                                            cookingTime = recipe.cookingTime,
                                                            ration = recipe.ration,
                                                            viewCount = recipe.viewCount,
                                                            createdAt = recipe.createdAt,
                                                            ingredients = ingredientsToSave,
                                                            steps = stepsToSave,
                                                            tags = tags,
                                                        )
                                                        markPainter = R.drawable.ic_mark_filled
                                                        isLoading = false
                                                    }
                                                } else {
                                                    isLoading = false
                                                    Toast.makeText(context, "Vui lòng thử lại", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Đã lưu", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(24.dp)
                    ) {
                        Icon(
                            painter = if (!isHistory) painterResource(markPainter) else painterResource(R.drawable.ic_mark_filled),
                            contentDescription = "Mark",
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            val shareText = """
${recipe.recipeName}
${recipe.likeQuantity} yêu thích, ${recipe.viewCount} lượt xem, ${recipe.comments.size} bình luận, ${recipe.cookingTime} chế biến

Nguyên liệu:
${recipe.ingredients.joinToString("\n") { ingredient ->
    "${ingredient.ingredientName} - ${ingredient.weight} ${ingredient.unit}"
}}

Cách thực hiện:
${recipe.cookingSteps.joinToString("\n") { step ->
    "${step.indexStep}. ${step.stepContent}"
}}

${if (recipe.image.startsWith("/")) "${ApiConstant.MAIN_URL}${recipe.image}" else ""}

Tác giả: ${recipe.userName}
                            """
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }

                            try {
                                context.startActivity(Intent.createChooser(intent, "Chia sẻ qua"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Vui lòng thử lại", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = "Share",
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .padding(bottom = 8.dp)
        )
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + slideInVertically() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .fillMaxWidth(0.9f)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
            )
        }
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn() + slideInVertically() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .fillMaxWidth(0.9f)
        ) {
            Image(
                painter = if (recipe.image.startsWith("/")) rememberAsyncImagePainter("${ApiConstant.MAIN_URL}${recipe.image}") else rememberAsyncImagePainter(model = recipe.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.9f)
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
        Text(
            text = recipe.recipeName,
            color = Color(0xFF000000),
            fontSize = 24.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.9f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.9f)
        ) {
            Image(
                painter = painterResource(R.drawable.img_common_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = recipe.userName,
                color = Color(0xFF555555),
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                modifier = Modifier
                    .padding(start = 8.dp)
            )
            Button(
                onClick = {},
                contentPadding = PaddingValues(vertical = 0.dp, horizontal = 8.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFADAEBC)
                ),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .height(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier
                            .size(16.dp)
                    )
                    Text(
                        text = "Theo dõi",
                        color = Color(0xFFFFFFFF),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                        modifier = Modifier
                            .padding(start = 4.dp)
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.9f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        if (!isLiked) {
                            recipe.recipeId?.let {
                                if (isLoggedIn) {
                                    userId?.let {
                                        coroutineScope.launch {
                                            val response = ApiClient.likeRecipe(recipeId = recipe.recipeId, userId = userId!!)
                                            if (response != null) {
                                                if (response.success) {
                                                    if (response.data != null) {
                                                        isLiked = true
                                                        likeQuantity = response.data.count
                                                    } else {
                                                        Log.e("RecipeViewScreen", "Error: ${response.message}")
                                                        Toast.makeText(context, "Bạn đã yêu thích công thức này", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Log.e("RecipeViewScreen", "Error: ${response.message}")
                                                    Toast.makeText(context, "Bạn đã yêu thích công thức này", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Log.e("RecipeViewScreen", "Error: Response is null")
                                                Toast.makeText(context, "Vui lòng thử lại", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Vui lòng đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_like_filled),
                    contentDescription = "Like",
                    tint = if (isLiked) Color(0xFFEF4444) else Color(0xFFCFCDCD),
                    modifier = Modifier
                        .size(24.dp)
                )
                Text(
                    text = CommonHelper.parseNumber(likeQuantity),
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                    fontWeight = FontWeight(400),
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_view_filled),
                contentDescription = "Like",
                tint = Color(0xFFFB923C),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(16.dp)
            )
            Text(
                text = CommonHelper.parseNumber(viewQuantity),
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .padding(start = 4.dp)
            )
            if (!isHistory) {
                Icon(
                    painter = painterResource(R.drawable.ic_comment_filled),
                    contentDescription = "Like",
                    tint = Color(0xFFFB923C),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(16.dp)
                )
                Text(
                    text = CommonHelper.parseNumber(commentQuantity),
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                    fontWeight = FontWeight(400),
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_clock_filled),
                contentDescription = "Like",
                tint = Color(0xFFFB923C),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(16.dp)
            )
            Text(
                text = recipe.cookingTime,
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                fontWeight = FontWeight(400),
                modifier = Modifier
                    .padding(start = 4.dp)
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.9f)
        ) {
            Text(
                text = "Tag${if (recipe.tags.size > 1) "s" else ""}: ",
                color = Color(0xFF555555),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                fontWeight = FontWeight(400),
            )
            Text(
                text = if(recipe.tags.isEmpty()) "Không có" else recipe.tags.joinToString(", ") { it.tagName },
                color = Color(0xFF000000),
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                fontWeight = FontWeight(400),
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(0.9f)
                .height(40.dp)
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val horizontalGuideline = createGuidelineFromStart(animateFooter)

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.2f)
                        .fillMaxHeight(0.2f)
                        .background(
                            color = Color(0xFFFB923C),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .constrainAs(createRef()) {
                            start.linkTo(horizontalGuideline)
                            end.linkTo(horizontalGuideline)
                            bottom.linkTo(parent.bottom)
                        }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val pages = listOf(
                    "Nguyên liệu",
                    "Cách thực hiện"
                )
                pages.forEachIndexed { index, page ->
                    Card(
                        onClick = {
                            coroutineScope.launch {
                                lazyListState.scrollToItem(index = index + 1)
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Text(
                                text = page,
                                color = if (selectedPage == index) Color(0xFF1B1B1D) else Color(0xFF5A5A60),
                                fontSize = 14.sp,
                                fontWeight = FontWeight(400),
                                fontFamily = FontFamily(Font(resId = R.font.roboto_medium))
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFE1E1E3),
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            state = lazyListState,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            item { Spacer(modifier = Modifier.height((1.dp))) }
            item { IngredientsView(recipe) }
            item { StepsView(recipe) }
            if (!isHistory) {
                item {
                    CommentsView(
                        comments = comments.asReversed(),
                        screenWidth = screenWidth,
                        onComment = { commentContent ->
                            recipe.recipeId?.let {
                                coroutineScope.launch {
                                    val response = ApiClient.commentRecipe(recipeId = recipe.recipeId, content = commentContent)
                                    if (response != null) {
                                        if (response.success) {
                                            if (response.data != null) {
                                                commentQuantity = response.data.count
                                                if (response.data.comments != null) {
                                                    comments.clear()
                                                    comments.addAll(response.data.comments)
                                                }
                                            } else {
                                                Log.e("RecipeViewScreen", "Error: ${response.message}")
                                                Toast.makeText(context, "Vui lòng thử lại", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Log.e("RecipeViewScreen", "Error: ${response.message}")
                                            Toast.makeText(context, "Vui lòng thử lại", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Log.e("RecipeViewScreen", "Error: Response is null")
                                        Toast.makeText(context, "Vui lòng thử lại", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun IngredientsView(recipe: Recipe) {
    Text(
        text = "Nguyên liệu",
        color = Color(0xFF000000),
        fontSize = 20.sp,
        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(0.9f)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth(0.9f)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_person),
            contentDescription = "Ration",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier
                .size(14.dp)
        )
        Text(
            text = "${recipe.ration} người ăn",
            color = Color(0xFF9CA3AF),
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
            modifier = Modifier
                .padding(start = 4.dp)
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 2.dp)
            .fillMaxWidth(0.9f)
    ) {
        recipe.ingredients.forEach { ingredient ->
            IngredientLine(ingredient)
        }
    }
}

@Composable
fun IngredientLine(ingredient: IngredientItem) {
    Text(
        text = "${ingredient.ingredientName} - ${ingredient.weight} ${ingredient.unit}",
        color = Color(0xFF000000),
        fontSize = 14.sp,
        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth(0.95f)
            .height(26.dp)
            .bottomDashedBorder()
    )
}

@Composable
fun StepsView(recipe: Recipe) {
    Text(
        text = "Cách thực hiện",
        color = Color(0xFF000000),
        fontSize = 20.sp,
        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
        modifier = Modifier
            .padding(top = 24.dp)
            .fillMaxWidth(0.9f)
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(0.9f)
    ) {
        recipe.cookingSteps.forEach { cookingStep ->
            CookingStepItem(cookingStep)
        }
    }
}

@Composable
fun CookingStepItem(cookingStep: CookingStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
    ) {
        Text(
            text = "Bước ${cookingStep.indexStep}",
            color = Color(0xFF000000),
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
            modifier = Modifier
                .padding(top = 8.dp)
        )
        Text(
            text = cookingStep.stepContent,
            color = Color(0xFF000000),
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            textAlign = TextAlign.Justify,
            modifier = Modifier
                .padding(top = 2.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun CommentsView(
    comments: List<CommentItem>,
    screenWidth: Int,
    onComment: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    var commentContent by remember { mutableStateOf("") }

    val animateIconExpand by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "Icon Expand"
    )
    val animateSendButton by animateFloatAsState(
        targetValue = if (commentContent != "") 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "Send Button"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth(0.9f)
        ) {
            Text(
                text = "Bình luận",
                color = Color(0xFF000000),
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                modifier = Modifier
                    .padding(top = 16.dp)
            )
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expand),
                    contentDescription = "Expand",
                    tint = Color(0xFF000000),
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(animateIconExpand)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(top = 4.dp, bottom = 8.dp)
                .fillMaxWidth()
                .padding(horizontal = screenWidth.dp * 0.05f, vertical = 12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.img_common_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterVertically)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                TextField(
                    value = commentContent,
                    onValueChange = { commentContent = it },
                    placeholder = { Text(text = "Thêm bình luận") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFFFFF),
                        unfocusedContainerColor = Color(0xFFFFFFFF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = CircleShape,
                    modifier = Modifier
//                        .padding(bottom = 16.dp)
//                        .fillMaxWidth()
                        .weight(1f)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape
                        )
                        .animateContentSize()
                )
                if (commentContent != "") {
                    IconButton(
                        onClick = {
                            onComment(commentContent)
                            commentContent = ""
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = "Send",
                            tint = Color(0xFF5164F3),
                            modifier = Modifier
                                .size(24.dp * animateSendButton)
                        )
                    }
                }
            }
        }
        if (isExpanded) {
            comments.forEach { comment ->
                CommentItemView(comment)
            }
        }
    }
}

@Composable
fun CommentItemView(comment: CommentItem, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth(0.9f)
            .background(color = Color(0xFFE5E7EB), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.img_common_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = comment.userName,
                    color = Color(0xFF000000),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                )
                Text(
                    text = CommonHelper.parseTime(comment.createdAt),
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
            }
        }
        Text(
            text = comment.content,
            color = Color(0xFF000000),
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            textAlign = TextAlign.Justify,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(0.9f)
        )
    }
}

fun Modifier.bottomDashedBorder(
    color: Color = Color(0xFFCFCDCD),
    strokeWidth: Dp = 1.dp,
    dashLength: Float = 20f,
    gapLength: Float = 10f
): Modifier = this.then(
    Modifier.drawBehind {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength))
        )

        val y = size.height - strokeWidth.toPx() / 2
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, y),
            end = androidx.compose.ui.geometry.Offset(size.width, y),
            strokeWidth = stroke.width,
            pathEffect = stroke.pathEffect
        )
    }
)

@Preview
@Composable
fun RecipeViewPreview() {
    val navController = rememberNavController()
    val database = AppDatabase.getDatabase(LocalContext.current)
    val viewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModel.Factory(
            repository = RecipeRepository(database.recipeDao(), database.ingredientDao(), database.tagDao())
        )
    )
    
    val comments = listOf(
        CommentItem(
            commentId = 1,
            userId = 1,
            userName = "nguyenvana",
            content = "Ngon tuyệt! Mình làm thử và thành công ngay lần đầu.",
            createdAt = "2025-06-15 10:20:00"
        ),
        CommentItem(
            commentId = 1,
            userId = 1,
            userName = "tranthib",
            content = "Cảm ơn công thức! Cả nhà mình đều thích.",
            createdAt = "2025-06-15 12:45:00"
        )
    )

    val recipe = Recipe(
        recipeId = 0,
        image = "https://umbercoffee.vn/wp-content/uploads/2024/06/matcha-latte-umber-coffee-tea-ho-chi-minh-city-700000.jpg",
        recipeName = "Phở bò Hà Nội",
        userName = "duonghung99",
        likeQuantity = 150,
        viewCount = 3200,
        ingredients = listOf(
            IngredientItem(ingredientId = 1, ingredientName = "Thịt bò", weight = 500, unit = "g"),
            IngredientItem(ingredientId = 2, ingredientName = "Bánh phở", weight = 200, unit = "g"),
            IngredientItem(ingredientId = 3, ingredientName = "Hành tây", weight = 1, unit = "củ"),
            IngredientItem(ingredientId = 4, ingredientName = "Quế", weight = 5, unit = "g"),
            IngredientItem(ingredientId = 5, ingredientName = "Hoa hồi", weight = 2, unit = "cái"),
            IngredientItem(ingredientId = 6, ingredientName = "Gừng", weight = 1, unit = "nhánh"),
            IngredientItem(ingredientId = 7, ingredientName = "Muối", weight = 1, unit = "muỗng cà phê"),
            IngredientItem(ingredientId = 8, ingredientName = "Nước mắm", weight = 2, unit = "muỗng canh"),
        ),
        cookingSteps = listOf(
            CookingStep(indexStep = 1, stepContent = "Nướng hành và gừng cho thơm."),
            CookingStep(indexStep = 2, stepContent = "Luộc thịt bò, vớt bọt."),
            CookingStep(indexStep = 3, stepContent = "Thêm hành, gừng, quế, hồi vào nồi."),
            CookingStep(indexStep = 4, stepContent = "Nêm nếm gia vị vừa ăn."),
            CookingStep(indexStep = 5, stepContent = "Trụng bánh phở, xếp ra tô, chan nước dùng.")
        ),
        cookingTime = "45 phút",
        ration = 4,
        isLiked = false,
        comments = comments,
        createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
        tags = listOf(TagEntity(tagId = 1, tagName = "Ăn vặt")),
        userId = 0
    )

    RecipeViewScreen(navController, recipe, false, recipeViewModel = viewModel)
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .fillMaxSize()
//            .background(color = Color(0xFFFFFFFF))
//    ) {
////            StepsView(recipe)
////            CommentsView(comments, 390)
//    }
}