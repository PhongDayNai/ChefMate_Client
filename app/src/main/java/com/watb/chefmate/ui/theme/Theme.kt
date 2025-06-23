package com.watb.chefmate.ui.theme

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiConstant
import com.watb.chefmate.data.AppConstant
import com.watb.chefmate.data.CommentItem
import com.watb.chefmate.data.CookingStep
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.database.entities.TagEntity
import com.watb.chefmate.helper.CommonHelper
import java.util.Date
import java.util.Locale

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ChefMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun Header(
    text: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(brush = AppConstant.headerGradient)
            .padding(16.dp),
    ) {
        leadingIcon?.invoke()
        text?.let {
            Text(
                text = text,
                color = Color(0xFFFFFFFF),
                fontSize = 24.sp,
                fontWeight = FontWeight.W700,
                modifier = Modifier
                    .padding(start = if (leadingIcon != null) 8.dp else 0.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        trailingIcon?.invoke()
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    placeholderSize: TextUnit = 16.sp,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val isFocused = remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(44.dp)
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            if (value == "") {
                Text(
                    text = placeholder,
                    color = Color(0xFFADAEBC),
                    fontSize = placeholderSize,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
                    fontWeight = FontWeight(400),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color(0xFF000000),
                    fontSize = placeholderSize
                ),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                maxLines = 1,
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            innerTextField()
                        }
                        if (trailingIcon != null) {
                            trailingIcon()
                        }
                    }
                },
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused.value = focusState.isFocused
                    }
            )
        }
    }
}

@Composable
fun RecipeItem(
    onClick: (Recipe) -> Unit,
    onEdit: (Recipe) -> Unit = {},
    onDelete: (Recipe) -> Unit = {},
    recipe: Recipe,
    isStorage: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick(recipe) },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = modifier
            .padding(top = 16.dp)
            .fillMaxWidth(0.9f)
    ) {
        Column {
            val painter = if (recipe.image.isNotBlank()) {
                if (recipe.image.startsWith("/")) {
                    rememberAsyncImagePainter("${ApiConstant.MAIN_URL}${recipe.image}")
                } else {
                    rememberAsyncImagePainter(recipe.image)
                }
            } else {
                painterResource(R.drawable.placeholder_image)
            }
            Image(
                painter = painter,
                contentDescription = recipe.recipeName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.recipeName,
                    color = Color(0xFF000000),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    fontWeight = FontWeight(700),
                )
                Text(
                    text = recipe.userName,
                    color = Color(0xFFFB923C),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                    fontWeight = FontWeight(400),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(color = Color(0xFFFFEDD5), RoundedCornerShape(4.dp))
                        .padding(4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                ) {
                    if (!isStorage) {
                        Icon(
                            painter = painterResource(R.drawable.ic_like_filled),
                            contentDescription = "Like",
                            tint = Color(0xFFEF4444) ,
                            modifier = Modifier
                                .size(16.dp)
                        )
                        Text(
                            text = CommonHelper.parseNumber(recipe.likeQuantity),
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            fontWeight = FontWeight(400),
                            modifier = Modifier
                                .padding(start = 4.dp)
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_view_filled),
                            contentDescription = "Like",
                            tint = Color(0xFFFB923C),
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(16.dp)
                        )
                        Text(
                            text = CommonHelper.parseNumber(recipe.viewCount),
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            fontWeight = FontWeight(400),
                            modifier = Modifier
                                .padding(start = 4.dp)
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_comment_filled),
                            contentDescription = "Like",
                            tint = Color(0xFFFB923C),
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(16.dp)
                        )
                        Text(
                            text = CommonHelper.parseNumber(recipe.comments.size),
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
                            .padding(start = if (isStorage) 0.dp else 16.dp)
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
                    Spacer(modifier = Modifier.weight(1f))
                    if (isStorage) {
                        SecondaryTextButtonTheme(
                            onClick = { onEdit(recipe) },
                            text = "Sửa",
                            paddingOuter = 0.dp,
                            paddingInner = 4.dp,
                            modifier = Modifier
                        )
                        SecondaryTextButtonTheme(
                            onClick = { onDelete(recipe) },
                            text = "Xóa",
                            paddingOuter = 0.dp,
                            paddingInner = 4.dp,
                            modifier = Modifier
                                .padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrimaryTextButtonTheme(
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    borderWidth: Int = 0,
    borderColor: Color = Color.Transparent,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(8.dp)
            .clickable(
                onClick = { if (enabled) onClick() }
            )
            .background(
                brush = if (enabled) AppConstant.onPrimaryGradient else AppConstant.unselectedPrimaryGradient,
                shape = shape
            )
            .border(width = borderWidth.dp, color = borderColor, shape = shape)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFFFFFFF),
            fontSize = 16.sp,
            fontWeight = FontWeight(600),
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
        )
    }
}

@Composable
fun SecondaryTextButtonTheme(
    onClick: () -> Unit,
    text: String,
    shape: Shape = CircleShape,
    paddingOuter: Dp = 8.dp,
    paddingInner: Dp = 16.dp,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val backgroundGradient = Brush.horizontalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF16A34A),
            1.0f to Color(0xFF16A34A)
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(paddingOuter)
            .clickable(onClick = onClick)
            .border(width = 1.dp, brush = backgroundGradient, shape = shape)
            .padding(paddingInner)
    ) {
        Text(
            text = text,
            color = Color(0xFFFF7121),
            fontSize = 16.sp,
            fontWeight = FontWeight(600),
            fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
        )
    }
}

@Composable
fun CustomDialog(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    isConfirm: Boolean,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    buttonText: String
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF97518))
                        .padding(12.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                    Spacer( modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cancel),
                            contentDescription = "cancel",
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (isConfirm) {
                        Text(
                            text = confirmText,
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            fontWeight = FontWeight(400),
                            modifier = Modifier
                                .padding(start = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Tên nguyên liệu",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            fontWeight = FontWeight(400),
                            modifier = Modifier
                                .padding(start = 4.dp)
                        )
                        CustomTextField(
                            value = name,
                            onValueChange = onNameChange,
                            placeholder = "Tên nguyên liệu",
                            placeholderSize = 12.sp,
                            modifier = Modifier
                                .padding(top = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Định lượng",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            fontWeight = FontWeight(400),
                            modifier = Modifier
                                .padding(start = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .padding(top = 12.dp)
                        ) {
                            CustomTextField(
                                value = weight,
                                onValueChange = onWeightChange,
                                placeholder = "Khối lượng",
                                placeholderSize = 12.sp,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .weight(1f)
                            )
                            CustomTextField(
                                value = unit,
                                onValueChange = onUnitChange,
                                placeholder = "Đơn vị",
                                placeholderSize = 12.sp,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .weight(1f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF97518)
                        )
                    ) {
                        Text(text = buttonText)
                    }
                }
            }
        }
    }
}

@Composable
fun CircularLoading(color: Color = Color.Black.copy(alpha = 0.75f), modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    color = Color.Green,
                    modifier = Modifier.size(150.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    var searchValue by remember { mutableStateOf("") }

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
        comments = listOf(
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
        ),
        createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
        tags = listOf(TagEntity(tagId = 1, tagName = "Ăn vặt")),
        userId = 0
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
    ) {
        Header(
            text = "Nấu ngon",
            trailingIcon = {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bell),
                        contentDescription = "Notification",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
        )
        CustomTextField(
            value = searchValue,
            onValueChange = { searchValue = it },
            placeholder = "Tìm kiếm món ăn",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Search",
                    tint = Color(0xFFFF9800).copy(alpha = 0.75f),
                    modifier = Modifier
                        .size(24.dp)
                )
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.9f)
        )
        RecipeItem(
            onClick = {},
            recipe = recipe,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.9f)
        )
    }
}