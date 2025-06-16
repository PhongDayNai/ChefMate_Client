package com.watb.chefmate.ui.recipe

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.watb.chefmate.R
import com.watb.chefmate.data.IngredientInput
import com.watb.chefmate.data.StepInput
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import kotlin.text.toIntOrNull

@Composable
fun AddRecipeScreen(
    navController: NavController,
    recipeId: Int = -1,
    viewModel: RecipeViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val nameRecipe = remember { mutableStateOf("") }
    val cookTime = remember { mutableStateOf("") }
    val ration = remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val ingredients = remember {
        mutableStateListOf(IngredientInput("", "", ""))
    }
    val steps = remember {
        mutableStateListOf(StepInput(1, ""))
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

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFB923C))
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
                }
            ) {
                Icon(
                    painterResource(R.drawable.ic_back),
                    contentDescription = "back",
                    tint = Color(0xFFFFFFFF)
                )
            }
            Text(
                text = "Thêm công thức",
                fontSize = 20.sp,
                color = Color(0xFFFFFFFF),
                fontWeight = FontWeight(600),
                modifier = Modifier
                    .padding(start = 16.dp)
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
                    top.linkTo(parent.top, margin = 80.dp)
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Image(
                painter = if (imageUri != null) rememberAsyncImagePainter(model = imageUri) else painterResource(R.drawable.placeholder_image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(180.dp)
                    .clickable { galleryLauncher.launch(arrayOf("image/*")) }
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
            )

            // Recipe Name
            OutlinedTextField(
                value = nameRecipe.value,
                onValueChange = { nameRecipe.value = it },
                label = { Text(text = "Tên công thức") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Cook Time & Ration
            Row(
                modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(20.dp))
                OutlinedTextField(
                    value = ration.value,
                    onValueChange = { ration.value = it},
                    label = { Text(text = "Khẩu phần ăn") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Is Public Radio Buttons
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Trạng thái",
                    fontWeight = FontWeight(600),
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isPublic,
                        onClick = { isPublic = true },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFB923C))
                    )
                    Text(text = "Công khai")
                    Spacer(modifier = Modifier.width(40.dp))
                    RadioButton(
                        selected = !isPublic,
                        onClick = { isPublic = false },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFB923C))
                    )
                    Text(text = "Riêng tư")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Ingredients Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nguyên liệu",
                    fontWeight = FontWeight(600),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                ingredients.forEachIndexed { index, ingredient ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = ingredient.name,
                            onValueChange = { ingredients[index] = ingredient.copy(name = it) },
                            label = { Text(text = "Tên nguyên liệu", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(4f)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        OutlinedTextField(
                            value = ingredient.weight,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                    ingredients[index] = ingredient.copy(weight = newValue)
                                }
                            },
                            label = { Text(text = "Khối lượng", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(3f)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        OutlinedTextField(
                            value = ingredient.unit,
                            onValueChange = { ingredients[index] = ingredient.copy(unit = it) },
                            label = { Text(text = "Đơn vị", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(2f)
                        )
                        if (ingredients.size > 1) { // Chỉ hiển thị nút xóa nếu có hơn 1 trường
                            IconButton(onClick = { ingredients.removeAt(index) }) {
                                Icon(
                                    painterResource(R.drawable.ic_minus), // Bạn cần thêm icon xóa (ic_delete.xml) vào drawable
                                    contentDescription = "Xóa nguyên liệu",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { ingredients.add(IngredientInput("", "", "")) }
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
            Spacer(modifier = Modifier.height(20.dp))

            // Steps Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Các bước nấu",
                    fontWeight = FontWeight(600),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                steps.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = step.content, // Index tự động tăng
                            onValueChange = { steps[index] = step.copy(content = it) },
                            label = { Text(text = "Bước ${index + 1}", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        if (steps.size > 1) { // Chỉ hiển thị nút xóa nếu có hơn 1 trường
                            IconButton(onClick = { steps.removeAt(index) }) {
                                Icon(
                                    painterResource(R.drawable.ic_minus), // Bạn cần thêm icon xóa (ic_delete.xml) vào drawable
                                    contentDescription = "Xóa bước",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
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
            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    scope.launch {
                        val parsedRation = ration.value.toIntOrNull() ?: 0 // Chuyển đổi ration sang Int

                        val ingredientsToSave = ingredients.filter { it.name.isNotBlank() && it.weight.isNotBlank() }.map {
                            Pair(it.name, Pair(it.weight.toIntOrNull() ?: 0, it.unit))
                        }
                        val stepsToSave = steps.filter { it.content.isNotBlank() }.map {
                            Pair(it.index, it.content)
                        }

                        if (recipeId == -1) {
                            Log.d("Uri", imageUri.toString())
                            viewModel.addRecipe(
                                recipeName = nameRecipe.value,
                                imageUri = imageUri?.toString() ?: "",
                                userName = "Thanh",
                                isPublic = isPublic,
                                likeQuantity = 0,
                                cookingTime = cookTime.value,
                                ration = parsedRation,
                                viewCount = 0,
                                createdAt = "",
                                ingredients = ingredientsToSave,
                                steps = stepsToSave
                            )
                        }
                        navController.popBackStack() // Quay lại màn hình danh sách công thức sau khi thêm/sửa
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF97518),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp) // Thêm padding dưới cùng cho nút
            ) {
                Text(
                    text = "Đăng công thức"
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}


@Preview
@Composable
fun AddRecipeScreensPreview() {
    val navController = rememberNavController()
    val viewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModel.Factory(
            repository = RecipeRepository(AppDatabase.getDatabase(LocalContext.current).recipeDao())
        )
    )
    AddRecipeScreen(navController, viewModel = viewModel)
}

