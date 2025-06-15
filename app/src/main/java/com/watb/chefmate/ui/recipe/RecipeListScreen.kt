package com.watb.chefmate.ui.recipe


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.watb.chefmate.R
import com.watb.chefmate.database.AppDatabase
import com.watb.chefmate.database.entities.Recipes
import com.watb.chefmate.repository.RecipeRepository
import com.watb.chefmate.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(navController: NavController, viewModel: RecipeViewModel = viewModel(
    factory = RecipeViewModel.Factory(
        repository = RecipeRepository(AppDatabase.getDatabase(LocalContext.current).recipeDao())
    )
)) {
    // Thu thập tất cả các công thức từ ViewModel dưới dạng State
    val recipes by viewModel.allRecipes.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kho công thức") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // Có thể thêm icon menu hoặc các hành động khác ở đây
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Mở Drawer hoặc hành động khác */ }) {
                        Icon(painterResource(R.drawable.ic_home), contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_edit_recipe_screen/0") // Điều hướng đến màn hình thêm công thức
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(painterResource(R.drawable.ic_add_recipe), "Thêm công thức mới")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        if (recipes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Chưa có công thức nào.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nhấn nút '+' để thêm công thức mới!", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp) // Thêm padding dưới để FAB không che mất item cuối
            ) {
                items(recipes) { recipe ->
                    RecipeCard(recipe = recipe, onClick = {
                        // Khi nhấn vào một công thức, bạn có thể điều hướng đến màn hình chi tiết hoặc màn hình chỉnh sửa
                        // Ví dụ: navController.navigate("recipe_detail_screen/${recipe.recipeId}")
                        // Hoặc điều hướng đến màn hình chỉnh sửa để xem/sửa chi tiết
                        navController.navigate("add_edit_recipe_screen/${recipe.recipeId}")
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipes, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (recipe.image != null) {
                Image(
                    bitmap = recipe.image.asImageBitmap(),
                    contentDescription = recipe.recipeName,
                    modifier = Modifier.size(90.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder image if no image is available
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Thay bằng placeholder của bạn
                    contentDescription = "No image available",
                    modifier = Modifier.size(90.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(text = recipe.recipeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Thời gian nấu: ${recipe.cookTime}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Khẩu phần: ${recipe.ration}", style = MaterialTheme.typography.bodySmall)
            }
        }
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Hiển thị ảnh sử dụng Coil
//            Image(
//                // Đảm bảo URI không null hoặc rỗng, sử dụng placeholder nếu cần
//                painter = rememberAsyncImagePainter(model = Uri.parse(recipe.image.ifEmpty { "android.resource://com.watb.chefmate/drawable/placeholder_image" })), // Thay 'placeholder_image' bằng tên drawable của bạn
//                contentDescription = recipe.recipeName,
//                modifier = Modifier.size(90.dp),
//                contentScale = ContentScale.Crop
//            )
//            Spacer(modifier = Modifier.size(16.dp))
//            Column {
//                Text(text = recipe.recipeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(text = "Thời gian nấu: ${recipe.cookTime}", style = MaterialTheme.typography.bodySmall)
//                Text(text = "Khẩu phần: ${recipe.ration}", style = MaterialTheme.typography.bodySmall)
//            }
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeListScreensPreview() {
    // Để preview màn hình này, bạn cần cung cấp NavController giả lập
    // và có thể một ViewModel giả lập nếu ViewModel có logic phức tạp
    RecipeListScreen(navController = rememberNavController())
}