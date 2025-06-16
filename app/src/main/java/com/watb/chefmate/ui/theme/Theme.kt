package com.watb.chefmate.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.watb.chefmate.R
import com.watb.chefmate.data.AppConstant
import com.watb.chefmate.data.Recipe
import com.watb.chefmate.helper.CommonHelper

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
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
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
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
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
                    fontSize = 16.sp,
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
                    fontSize = 16.sp
                ),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                cursorBrush = SolidColor(Color.White),
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
    recipe: Recipe,
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
                rememberAsyncImagePainter(recipe.image)
            } else {
                painterResource(R.drawable.placeholder_image)
            }
            Image(
                painter = painter,
                contentDescription = recipe.name,
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
                    text = recipe.name,
                    color = Color(0xFF000000),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    fontWeight = FontWeight(700),
                )
                Text(
                    text = recipe.author,
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
//                    Icon(
//                        painter = painterResource(R.drawable.ic_like),
//                        contentDescription = "Like",
//                        tint = Color(0xFFFB923C),
//                        modifier = Modifier
//                            .size(16.dp)
//                    )
//                    Text(
//                        text = CommonHelper.parseNumber(recipe.likesQuantity),
//                        color = Color(0xFF6B7280),
//                        fontSize = 14.sp,
//                        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
//                        fontWeight = FontWeight(400),
//                        modifier = Modifier
//                            .padding(start = 4.dp)
//                    )
//                    Icon(
//                        painter = painterResource(R.drawable.ic_view),
//                        contentDescription = "Like",
//                        tint = Color(0xFFFB923C),
//                        modifier = Modifier
//                            .padding(start = 16.dp)
//                            .size(16.dp)
//                    )
//                    Text(
//                        text = CommonHelper.parseNumber(recipe.viewCount),
//                        color = Color(0xFF6B7280),
//                        fontSize = 14.sp,
//                        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
//                        fontWeight = FontWeight(400),
//                        modifier = Modifier
//                            .padding(start = 4.dp)
//                    )
//                    Icon(
//                        painter = painterResource(R.drawable.ic_comment),
//                        contentDescription = "Like",
//                        tint = Color(0xFFFB923C),
//                        modifier = Modifier
//                            .padding(start = 16.dp)
//                            .size(16.dp)
//                    )
//                    Text(
//                        text = CommonHelper.parseNumber(recipe.comments.size),
//                        color = Color(0xFF6B7280),
//                        fontSize = 14.sp,
//                        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
//                        fontWeight = FontWeight(400),
//                        modifier = Modifier
//                            .padding(start = 4.dp)
//                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_like_filled),
                        contentDescription = "Like",
                        tint = if (recipe.isLiked) Color(0xFFEF4444) else Color(0xFFCFCDCD),
                        modifier = Modifier
                            .size(16.dp)
                    )
                    Text(
                        text = CommonHelper.parseNumber(recipe.likesQuantity),
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
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    var searchValue by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
    ) {
        Header("Nấu ngon") {
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
        SearchTextField(
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
    }
}