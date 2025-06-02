# Hệ thống ứng dụng công thức nấu ăn - ChefMate

# I. Client - Ứng dụng di động

- Công nghệ: Kotlin với thư viện Jetpack Compose
- Cơ sở dữ liệu: RoomDB

## 1. Tính năng

| User | Recipe | Interaction |
| --- | --- | --- |
| Đăng nhập | Hiển thị công thức nấu ăn | Yêu thích |
| Đăng ký | Tìm kiếm công thức | Bình luận |
| Đổi mật khẩu | Thêm vào “Công thức của tôi” | Chia sẻ |
| Đăng xuất | Lập danh sách mua sắm |  |
| Chỉnh sửa thông tin cá nhân | Quản lý danh sách mua sắm |  |
|  | Tạo công thức mới |  |
|  | Chỉnh sửa công thức trong “Công thức của tôi” |  |
|  | Xóa khỏi “Công thức của tôi” |  |
|  | Lịch sử xem công thức |  |
|  | Lịch sử mua sắm |  |
|  | Lấy danh sách công thức Top Trending |  |
|  | Lấy danh sách nguyên liệu sẵn có |  |

## 2. Cơ sở dữ liệu

- Bảng Recipes

| Tên cột | Kiểu dữ liệu | Khóa/Ràng buộc | Mô tả |
| --- | --- | --- | --- |
| `recipeId`  | `INT` | `PRIMARY KEY`  | Mã định danh duy nhất |
|  |  | `IDENTITY(1, 1)`  | Tự động tăng từ 1 |
| `recipeName` | `NVARCHAR(100)` | `NOT NULL` | Tên của công thức |
| `image`  | `NVARCHAR(1000)`  | `NOT NULL`  | URL hình ảnh |
- Bảng Ingredients

| Tên cột | Kiểu dữ liệu | Khóa/Ràng buộc | Mô tả |
| --- | --- | --- | --- |
| `ingredientId`  | `INT`  | `PRIMARY KEY`  | Mã định danh duy nhất |
|  |  | `IDENTITY(1, 1)`  | Tự động tăng từ 1 |
| `ingredientName`  | `NVARCHAR(100)`  | `NOT NULL`  | Tên của nguyên liệu |
- Bảng RecipesIngredients

| Tên cột | Kiểu dữ liệu | Khóa/Ràng buộc | Mô tả |
| --- | --- | --- | --- |
| `riId`  | `INT`  | `PRIMARY KEY`  | Mã định danh duy nhất |
|  |  | `IDENTITY(1, 1)`  | Tự động tăng từ 1 |
| `recipeId`  | `INT`  | `FOREIGN KEY REFERENCES Recipes(recipeId)` | Liên kết dến bảng `Recipes` |
| `ingredientId`  | `INT`  | `FOREIGN KEY REFERENCES Ingredients(ingredientId)`  | Liên kết đến bảng `Ingredients` |
| `weight`  | `INT`  | `NOT NULL`  | Số lượng/Khối lượng của nguyên liệu |
| `unit`  | `NVARCHAR(20)`  | `NOT NULL`  | Đơn vị tính |
- Bảng ShoppingTimes
- Bảng ShoppingRecipes
- Bảng ShoppingIngredients
