package com.watb.chefmate.ui.recipe

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.watb.chefmate.R
import com.watb.chefmate.api.ApiClient
import com.watb.chefmate.data.AppConstant
import com.watb.chefmate.data.CookingStepAddRecipeData
import com.watb.chefmate.data.CreateRecipeData
import com.watb.chefmate.data.IngredientInput
import com.watb.chefmate.data.IngredientItem
import com.watb.chefmate.data.StepInput
import com.watb.chefmate.data.TagData
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.database.entities.IngredientEntity
import com.watb.chefmate.database.entities.TagEntity
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.ui.theme.CircularLoading
import com.watb.chefmate.viewmodel.RecipeViewModel
import com.watb.chefmate.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.toIntOrNull

@SuppressLint("MemberExtensionConflict")
@Composable
fun AddOrEditRecipeScreen(
    navController: NavController,
    recipeId: Int = -1,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val user by userViewModel.user.collectAsState()
    val nameRecipe = remember { mutableStateOf("") }
    val cookTime = remember { mutableStateOf("") }
    val ration = remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val selectedUnit = remember { mutableStateOf("Phút") }
    val userName = remember { mutableStateOf("") }
    val tagsInput = remember { mutableStateOf("") }
    var isShownAddTagDialog by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf("") }

    val ingredients = remember {
        mutableStateListOf(IngredientInput("", "", ""))
    }
    val steps = remember {
        mutableStateListOf(StepInput(1, ""))
    }
    val tags = remember {
        mutableStateListOf<String>()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    LaunchedEffect(recipeId) {
        if (recipeId != -1) {
            recipeViewModel.getRecipeById(recipeId).collect { recipe ->
                recipe?.let {
                    nameRecipe.value = it.recipeName
                    imageUri = it.image.toUri()
                    cookTime.value = it.cookingTime.replace(" Phút", "").replace(" Giờ", "")
                    ration.value = it.ration.toString()
                    isPublic = false
                    userName.value = it.userName

                    ingredients.clear()
                    it.ingredients.forEach { ingredientItem ->
                        ingredients.add(
                            IngredientInput(
                                name = ingredientItem.ingredientName,
                                weight = ingredientItem.weight.toString(),
                                unit = ingredientItem.unit
                            )
                        )
                    }

                    steps.clear()
                    it.cookingSteps.forEach { cookingStep ->
                        cookingStep.indexStep?.let {
                            steps.add(
                                StepInput(
                                    index = cookingStep.indexStep,
                                    content = cookingStep.stepContent
                                )
                            )
                        }
                    }
                    tagsInput.value = it.tags.joinToString(", ") { tag -> tag.tagName }
                }
            }
        }
    }

    val isFilled = imageUri.toString().isNotEmpty() && imageUri != null && nameRecipe.value.isNotEmpty() && cookTime.value.isNotEmpty() && ration.value.isNotEmpty() && ingredients.isNotEmpty() && steps.isNotEmpty()

    val focusManager = LocalFocusManager.current

    val cookTimeFocusRequester = remember { FocusRequester() }
    val rationFocusRequester = remember { FocusRequester() }
    val ingredientFocusRequesters = remember { mutableStateListOf<FocusRequester>() }
    val stepFocusRequesters = remember { mutableStateListOf<FocusRequester>() }

    LaunchedEffect(ingredients.size) {
        while (ingredientFocusRequesters.size < ingredients.size * 3) {
            ingredientFocusRequesters.add(FocusRequester())
        }
        while (ingredientFocusRequesters.size > ingredients.size * 3) {
            ingredientFocusRequesters.removeAt(ingredientFocusRequesters.size - 1)
        }
    }

    LaunchedEffect(steps.size) {
        while (stepFocusRequesters.size < steps.size) {
            stepFocusRequesters.add(FocusRequester())
        }
        while (stepFocusRequesters.size > steps.size) {
            stepFocusRequesters.removeAt(stepFocusRequesters.size - 1)
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = AppConstant.backgroundGradient)
            .safeDrawingPadding()
    ) {
        val (headerRef, contentRef) = createRefs()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .constrainAs(headerRef) {
                    top.linkTo(parent.top)
                }
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_back),
                    contentDescription = "back",
                    tint = Color(0xFFFFFFFF),
                    modifier = Modifier
                        .size(24.dp)
                )
            }
            Text(
                text =  if (recipeId == -1) "Thêm công thức" else "Sửa công thức",
                fontSize = 20.sp,
                color = Color(0xFFFFFFFF),
                fontWeight = FontWeight(600),
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                modifier = Modifier
                    .padding(start = 4.dp)
            )
        }
        Column(
            modifier = Modifier
                .background(
                    Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .fillMaxSize()
                .constrainAs(contentRef) {
                    top.linkTo(headerRef.bottom, margin = 8.dp)
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Image(
                painter = if (imageUri != null) rememberAsyncImagePainter(model = imageUri) else painterResource(R.drawable.ic_upload),
                contentDescription = null,
                contentScale = if (imageUri != null) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth(0.9f)
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .padding(if (imageUri != null) 0.dp else 40.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable { galleryLauncher.launch(arrayOf("image/*")) }
            )
            Text(
                text = "Tags",
                fontWeight = FontWeight(600),
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(top = 12.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { isShownAddTagDialog = true },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(32.dp)
                ) {
                    Text(
                        text = "Thêm",
                        color = Color(0xFFFB923C),
                        fontWeight = FontWeight(600),
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                        fontSize = 14.sp,
                        modifier = Modifier
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                ) {
                    if (tags.isEmpty()) {
                        Text(
                            text = "Chưa nhập tag nào",
                            color = Color(0xFF555555),
                            fontWeight = FontWeight(500),
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            fontSize = 14.sp,
                            modifier = Modifier
                        )
                    } else {
                        tags.forEach { tag ->
                            Text(
                                text = tag,
                                color = Color(0xFF000000),
                                fontWeight = FontWeight(500),
                                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .background(color = Color(0xFFFDBA74), shape = CircleShape)
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = nameRecipe.value,
                onValueChange = { nameRecipe.value = it },
                label = { Text(text = "Tên công thức") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(10.dp),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        cookTimeFocusRequester.requestFocus()
                    }
                ),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                OutlinedTextField(
                    value = cookTime.value,
                    onValueChange = { cookTime.value = it },
                    label = { Text(text = "Thời gian nấu") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { rationFocusRequester.requestFocus() }
                    ),
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .weight(1f)
                        .focusRequester(cookTimeFocusRequester)
                )
                TimeUnitDropdown(
                    selectedUnit = selectedUnit.value,
                    onUnitSelected = { selectedUnit.value = it },
                    modifier = Modifier
                        .weight(1f)
                )
            }
            OutlinedTextField(
                value = if (ration.value == "") ration.value else "${ration.value} người",
                onValueChange = {
                    val cleanInput = it.filter { char -> char.isDigit() }
                    ration.value = cleanInput
                },
                label = { Text(text = "Khẩu phần ăn") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .focusRequester(rationFocusRequester)
            )

            if (recipeId != -1) {
                Column(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Trạng thái",
                        fontWeight = FontWeight(600),
                        fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isPublic,
                            onClick = {
                                if (!isLoggedIn) {
                                    Toast.makeText(context, "Vui lòng đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show()
                                } else {
                                    isPublic = true
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFB923C))
                        )
                        Text(text = "Công khai")

                        RadioButton(
                            selected = !isPublic,
                            onClick = { isPublic = false },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFB923C)),
                            modifier = Modifier
                                .padding(start = 20.dp)
                        )
                        Text(text = "Riêng tư")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(
                    text = "Nguyên liệu",
                    fontWeight = FontWeight(600),
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    fontSize = 16.sp,
                )

                ingredients.forEachIndexed { index, ingredient ->
                    val nameIndex = index * 3
                    val weightIndex = index * 3 + 1
                    val unitIndex = index * 3 + 2

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        ),
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .background(Color(0xFFFFFFFF))
                    ) {
                        Column {
                            Row {
                                IngredientDropdown(
                                    selectedIngredient = ingredient.name,
                                    onIngredientSelected = { ingredients[index] = ingredient.copy(name = it) },
                                    recipeViewModel = recipeViewModel,
                                    onNext = {
                                        ingredientFocusRequesters.getOrNull(weightIndex)?.requestFocus()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(
                                            ingredientFocusRequesters.getOrNull(
                                                nameIndex
                                            ) ?: FocusRequester()
                                        )
                                )
                                if (ingredients.size > 1) {
                                    IconButton(
                                        onClick = {
                                            ingredients.removeAt(index)
                                            focusManager.clearFocus()
                                        }
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.ic_minus),
                                            contentDescription = "Xóa nguyên liệu",
                                            tint = Color.Red,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                color = Color(0xFFE0E0E0),
                                thickness = 1.dp,
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 64.dp)
                            )
                            Row {
                                TextField(
                                    value = ingredient.weight,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                            ingredients[index] = ingredient.copy(weight = newValue)
                                        }
                                    },
                                    label = { Text(text = "Khối lượng", fontSize = 12.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            ingredientFocusRequesters.getOrNull(unitIndex)?.requestFocus()
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .padding(start = 5.dp, end = 5.dp)
                                        .weight(1f)
                                        .focusRequester(
                                            ingredientFocusRequesters.getOrNull(
                                                weightIndex
                                            ) ?: FocusRequester()
                                        )
                                )
                                TextField(
                                    value = ingredient.unit,
                                    onValueChange = { ingredients[index] = ingredient.copy(unit = it) },
                                    label = { Text(text = "Đơn vị", fontSize = 12.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 1,
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(
                                            ingredientFocusRequesters.getOrNull(
                                                unitIndex
                                            ) ?: FocusRequester()
                                        )
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clickable {
                            ingredients.add(IngredientInput("", "", ""))
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                                ingredientFocusRequesters.getOrNull((ingredients.size - 1) * 3)
                                    ?.requestFocus()
                            }
                        }
                ) {
                    Icon(
                        painterResource(R.drawable.ic_add),
                        contentDescription = "Thêm nguyên liệu",
                        tint = Color(0xFFFB923C),
                        modifier = Modifier.size(26.dp)
                    )
                    Text(text = "Thêm nguyên liệu")
                }
            }

            Column(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Các bước nấu",
                    fontWeight = FontWeight(600),
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                )

                steps.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = step.content,
                            onValueChange = { steps[index] = step.copy(content = it) },
                            label = { Text(text = "Bước ${index + 1}", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    if (index < steps.size - 1) {
                                        stepFocusRequesters.getOrNull(index + 1)?.requestFocus()
                                    } else {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                }
                            ),
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .weight(1f)
                        )
                        if (steps.size > 1) {
                            IconButton(
                                onClick = {
                                    steps.removeAt(index)
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_minus),
                                    contentDescription = "Xóa bước",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { steps.add(StepInput(steps.size + 1, "")) }
                ) {
                    Icon(
                        painterResource(R.drawable.ic_add),
                        contentDescription = "Thêm bước nấu",
                        tint = Color(0xFFFB923C),
                        modifier = Modifier.size(26.dp)
                    )
                    Text(text = "Thêm bước nấu")
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (isFilled) {
                            isLoading = true
                            val parsedRation = ration.value.toIntOrNull() ?: 0

                            val ingredientsToSave = ingredients.filter { it.name.isNotBlank() && it.weight.isNotBlank() }.map {
                                Pair(it.name, Pair(it.weight.toIntOrNull() ?: 0, it.unit))
                            }
                            val stepsToSave = steps.filter { it.content.isNotBlank() }.map {
                                Pair(it.index, it.content)
                            }
                            val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
                            val tagsToSave = tagsInput.value.split(",").map { it.trim() }.filter { it.isNotBlank() }

                            if (recipeId == -1) {
                                Log.d("Uri", imageUri.toString())
                                if (!isPublic) {
                                    recipeViewModel.addRecipe(
                                        recipeName = nameRecipe.value,
                                        imageUri = imageUri?.toString() ?: "",
                                        userName = user?.fullName ?: "Người dùng",
                                        isPublic = isPublic,
                                        likeQuantity = 0,
                                        cookingTime = "${cookTime.value} ${selectedUnit.value}",
                                        ration = parsedRation,
                                        viewCount = 0,
                                        createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()),
                                        ingredients = ingredientsToSave,
                                        steps = stepsToSave,
                                        tags = tagsToSave
                                    )
                                    navController.popBackStack()
                                } else {
                                    if (user != null) {
                                        val ingredientItems = mutableListOf<IngredientItem>()
                                        val cookingStepItems = mutableListOf<CookingStepAddRecipeData>()
                                        val tagItems = mutableListOf<TagData>()
                                        ingredients.forEach { ingredient ->
                                            ingredientItems.add(IngredientItem(ingredientName = ingredient.name.trim(), weight = ingredient.weight.toIntOrNull() ?: 0, unit = ingredient.unit))
                                        }
                                        steps.forEach { cookingStep ->
                                            cookingStepItems.add(CookingStepAddRecipeData(content = cookingStep.content.trim()))
                                        }
                                        tags.forEach { tag ->
                                            tagItems.add(TagData(tagName = tag.trim()))
                                        }

                                        val recipe = CreateRecipeData(
                                            recipeName = nameRecipe.value.trim(),
                                            image = imageUri?.toString()?.trim() ?: "",
                                            userId = user!!.userId,
                                            cookingTime = "${cookTime.value} ${selectedUnit.value}",
                                            ration = parsedRation,
                                            ingredients = ingredientItems.toList(),
                                            cookingSteps = cookingStepItems.toList(),
                                            tags = tagItems.toList()
                                        )
                                        val response = ApiClient.createRecipe(context, recipe)
                                        if (response != null) {
                                            if (response.success) {
                                                recipeViewModel.addRecipe(
                                                    recipeName = nameRecipe.value.trim(),
                                                    imageUri = imageUri.toString().trim(),
                                                    userName = user?.fullName ?: "Người dùng",
                                                    isPublic = isPublic,
                                                    likeQuantity = 0,
                                                    cookingTime = "${cookTime.value} ${selectedUnit.value}",
                                                    ration = parsedRation,
                                                    viewCount = 0,
                                                    createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()),
                                                    ingredients = ingredientsToSave,
                                                    steps = stepsToSave,
                                                    tags = tagsToSave
                                                )
                                                navController.popBackStack()
                                            } else {
                                                Log.e("AddRecipeScreen", "Error: ${response.message}")
                                                Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Log.e("AddRecipeScreen", "Error: Response is null")
                                            Toast.makeText(context, "Có lỗi xảy ra. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                recipeViewModel.updateRecipe(
                                    recipeId = recipeId,
                                    recipeName = nameRecipe.value.trim(),
                                    imageUri = imageUri.toString().trim(),
                                    userName = userName.value.trim(),
                                    isPublic = false,
                                    likeQuantity = 0,
                                    cookingTime = "${cookTime.value} ${selectedUnit.value}",
                                    ration = parsedRation,
                                    viewCount = 0,
                                    createdAt = currentDate,
                                    ingredients = ingredientsToSave,
                                    steps = stepsToSave,
                                    tags = tagsToSave
                                )
                                navController.popBackStack()
                            }
                            isLoading = false
                        } else {
                            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF97518),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 60.dp)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = if (recipeId == -1) "Đăng công thức" else "Cập nhật công thức"
                )
            }
        }
        if (isLoading) {
            CircularLoading()
        }
        if (isShownAddTagDialog) {
            AddTagDialog(
                onDismiss = {
                    isShownAddTagDialog = false
                    selectedTag = ""
                },
                selectedTag = selectedTag,
                tags = tags,
                onTagSelected = { selectedTag = it },
                onAddNewTag = {
                    if (selectedTag.isNotBlank()) {
                        if (!tags.contains(selectedTag)) {
                            tags.add(selectedTag)
                            selectedTag = ""
                        } else {
                            Toast.makeText(context, "Tag đã được chọn", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Vui lòng nhập tag", Toast.LENGTH_SHORT).show()
                    }
                },
                recipeViewModel = recipeViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeUnitDropdown(selectedUnit: String, onUnitSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val expanded = remember { mutableStateOf(false) }
    val options = listOf("Giờ", "Phút")

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            label = { Text("Đơn vị") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded.value
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFFFFFF),
                unfocusedContainerColor = Color(0xFFFFFFFF),
                focusedIndicatorColor = Color(0xFFE0E0E0),
                unfocusedIndicatorColor = Color(0xFFE0E0E0)
            ),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

@SuppressLint("MemberExtensionConflict")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDropdown(
    selectedIngredient: String,
    onIngredientSelected: (String) -> Unit,
    recipeViewModel: RecipeViewModel,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expanded = remember { mutableStateOf(false) }
    val ingredients = remember { mutableStateListOf<IngredientEntity>() }
    val ingredientsSearch = remember { mutableStateListOf<IngredientEntity>() }

    LaunchedEffect(Unit) {
        recipeViewModel.getAllIngredients().collect { list ->
            list?.let {
                ingredients.clear()
                ingredients.addAll(it)
                ingredientsSearch.clear()
                ingredientsSearch.addAll(it)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = {
            if (ingredientsSearch.isNotEmpty()) {
                expanded.value = true
            } else {
                expanded.value = ingredients.isNotEmpty()
            }
        },
        modifier = modifier
    ) {
        TextField(
            value = selectedIngredient,
            onValueChange = { newValue ->
                onIngredientSelected(newValue)

                ingredientsSearch.clear()
                ingredientsSearch.addAll(
                    ingredients.filter {
                        it.ingredientName.contains(newValue, ignoreCase = true)
                    }
                )

                expanded.value = true
            },
            label = { Text(text = "Tên nguyên liệu", fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() }
            ),
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier
                .heightIn(max = 300.dp)
        ) {
            if (ingredientsSearch.isEmpty()) {
                if (selectedIngredient == "") {
                    ingredients.forEach { ingredient ->
                        DropdownMenuItem(
                            text = { Text(ingredient.ingredientName) },
                            onClick = {
                                onIngredientSelected(ingredient.ingredientName)
                                expanded.value = false
                            }
                        )
                    }
                }
            } else {
                ingredientsSearch.forEach { ingredient ->
                    DropdownMenuItem(
                        text = { Text(ingredient.ingredientName) },
                        onClick = {
                            onIngredientSelected(ingredient.ingredientName)
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddTagDialog(
    onDismiss: () -> Unit = {},
    selectedTag: String,
    tags: List<String>,
    onTagSelected: (String) -> Unit,
    onAddNewTag: () -> Unit,
    recipeViewModel: RecipeViewModel,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val (contentRef, closeButtonRef) = createRefs()

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(16.dp))
                    .constrainAs(contentRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(12.dp)
            ) {
                Text(
                    text = "Thêm tag",
                    fontWeight = FontWeight(600),
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold)),
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                )
                TagDropdown(
                    selectedTag = selectedTag,
                    onTagSelected = onTagSelected,
                    recipeViewModel = recipeViewModel,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    if (tags.isEmpty()) {
                        Text(
                            text = "Chưa nhập tag nào",
                            color = Color(0xFF555555),
                            fontWeight = FontWeight(500),
                            fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                            fontSize = 14.sp,
                            modifier = Modifier
                        )
                    } else {
                        tags.forEach { tag ->
                            Text(
                                text = tag,
                                color = Color(0xFF000000),
                                fontWeight = FontWeight(500),
                                fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .background(color = Color(0xAFFDBA74), shape = CircleShape)
                                    .padding(6.dp)
                            )
                        }
                    }
                }
                Button(
                    onClick = onAddNewTag,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFB923C)
                    ),
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(Alignment.End)
                ) {
                    Text(text = "Thêm")
                }
            }

            Button(
                onClick = onDismiss,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF)
                ),
                modifier = Modifier
                    .size(36.dp)
                    .constrainAs(closeButtonRef) {
                        top.linkTo(contentRef.bottom, margin = 24.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cancel),
                    contentDescription = "Close",
                    tint = Color(0xFFFB923C),
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
    }
}

@SuppressLint("MemberExtensionConflict")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagDropdown(
    selectedTag: String,
    onTagSelected: (String) -> Unit,
    recipeViewModel: RecipeViewModel,
    modifier: Modifier = Modifier
) {
    val expanded = remember { mutableStateOf(false) }
    val tags = remember { mutableStateListOf<TagEntity>() }
    val tagsSearch = remember { mutableStateListOf<TagEntity>() }

    LaunchedEffect(Unit) {
        recipeViewModel.getAllTags().collect { list ->
            list?.let {
                tags.clear()
                tags.addAll(it)
                tagsSearch.clear()
                tagsSearch.addAll(it)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = {
            if (tagsSearch.isNotEmpty()) {
                expanded.value = true
            } else {
                expanded.value = tags.isNotEmpty()
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedTag,
            onValueChange = { newValue ->
                onTagSelected(newValue)

                tagsSearch.clear()
                tagsSearch.addAll(
                    tags.filter {
                        it.tagName.contains(newValue, ignoreCase = true)
                    }
                )

                expanded.value = true
            },
            label = { Text(text = "Tên tag", fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE0E0E0),
                unfocusedBorderColor = Color(0xFFE0E0E0),
            ),
            keyboardActions = KeyboardActions(
                onDone = { onTagSelected(selectedTag) }
            ),
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier
                .heightIn(max = 300.dp)
        ) {
            if (tagsSearch.isEmpty()) {
                if (selectedTag == "") {
                    tags.forEach { tag ->
                        DropdownMenuItem(
                            text = { Text(tag.tagName) },
                            onClick = {
                                onTagSelected(tag.tagName)
                                expanded.value = false
                            }
                        )
                    }
                }
            } else {
                tagsSearch.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag.tagName) },
                        onClick = {
                            onTagSelected(tag.tagName)
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun AddRecipeScreensPreview() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()

    val database = AppDatabase.getDatabase(LocalContext.current)
    val recipeRepository = RecipeRepository(database.recipeDao(), database.ingredientDao(), database.tagDao())

    val recipeViewModel = RecipeViewModel(recipeRepository)

    AddOrEditRecipeScreen(navController, userViewModel = userViewModel, recipeViewModel = recipeViewModel)
}

