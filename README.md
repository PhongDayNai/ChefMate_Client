# Hệ thống ứng dụng công thức nấu ăn - ChefMate

# I. Client - Ứng dụng di động

- Công nghệ: Kotlin với thư viện Jetpack Compose
- Cơ sở dữ liệu: RoomDB

## 1. Tính năng

| User | Recipe | Interaction |
| --- | --- | --- |
| Đăng nhập | Hiển thị công thức nấu ăn | Yêu thích |
| Đăng ký | Tìm kiếm công thức theo tên | Bình luận |
| Đổi mật khẩu | Tìm kiếm công thức theo tag | Chia sẻ |
| Đăng xuất | Lưu vào “Kho công thức” |  |
| Chỉnh sửa thông tin cá nhân | Lập danh sách mua sắm |  |
|  | Quản lý danh sách mua sắm |  |
|  | Tạo công thức mới |  |
|  | Chỉnh sửa công thức trong “Kho công thức” |  |
|  | Xóa khỏi “Kho công thức” |  |
|  | Lịch sử đăng công thức |  |
|  | Lịch sử mua sắm |  |
|  | Lấy danh sách công thức Top Trending |  |
|  | Lấy danh sách nguyên liệu sẵn có |  |
|  | Lấy danh sách tag sẵn có |  |

### a. User

- [x]  Đăng nhập
- [x]  Đăng ký
- [x]  Đổi mật khẩu
- [x]  Đăng xuất
- [x]  Chỉnh sửa thông tin cá nhân

### b. Recipe

- [x]  Hiển thị công thức nấu ăn
- [x]  Tìm kiếm công thức theo tên
- [x]  Tìm kiếm công thức theo tag
- [x]  Thêm vào “Kho công thức”
- [x]  Lập danh sách mua sắm
- [x]  Quản lý danh sách mua sắm
- [x]  Tạo công thức mới
- [x]  Chỉnh sửa công thức trong “Kho công thức”
- [x]  Xóa khỏi “Kho công thức”
- [x]  Lịch sử đăng công thức
- [x]  Lịch sử mua sắm
- [x]  Lấy danh sách Top Trending
- [x]  Lấy danh sách nguyên liệu sẵn có
- [x]  Lấy danh sách tag sẵn có

### c. Interaction

- [x]  Yêu thích
- [x]  Bình luận
- [x]  Chia sẻ

## 2. Cơ sở dữ liệu

- Bảng Recipes


    | Tên cột | Kiểu dữ liệu | Khóa/Ràng buộc | Mô tả |
    | --- | --- | --- | --- |
    | `recipeId`  | `INT` | `PRIMARY KEY`  | Mã định danh duy nhất |
    |  |  | `IDENTITY(1, 1)`  | Tự động tăng từ 1 |
    | `recipeName` | `NVARCHAR(100)` | `NOT NULL` | Tên của công thức |
    | `image`  | `NVARCHAR(1000)`  | `NOT NULL`  | URL hình ảnh |
    | `userId` | `INT` | `FOREIGN KEY REFERENCES Users(userId)` | liên kết đến bảng Users |
    | `isPublic` | `BOOLEAN` | `NOT NULL` |  |
    | `likeQuantity` | `INT` | `NOT NULL` | lượt yêu thích |
    | `cookingTime` | `NVARCHAR(20)` | `NOT NULL` | thời gian nấu |
    | `ration` | `INT` | `NOT NULL` | khẩu phần ăn |
    | `viewCount` | `INT` | `NOT NULL` | lượt xem |
    | createAt | DATE | NOT NULL | ngày tạo công thức |
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


    | Tên cột | Kiểu dữ liệu | Khóa/Ràng buộc | Mô tả |
    | --- | --- | --- | --- |
    | `stId` | `INT` | `PRIMARY KEY` | Mã định danh duy nhất |
    | `shoppingDate` | `DATE` | `NOT NULL` | Ngày mua sắm |
- Bảng ShoppingRecipes


    | Tên cột | Kiểu dữ liệu | Khóa/Ràng buộc | Mô tả |
    | --- | --- | --- | --- |
    | `srId`  | `INT` | `PRIMARY KEY` | Mã định danh duy nhất |
    | `stId` | `INT` | `FOREIGN KEY REFERENCES ShoppingTimes(stId)` | liên kết đến bảng ShoppingTimes |
    | `recipeId` | `INT` | `FOREIGN KEY REFERENCES Recipes(recipeId)` | liên kết đến bảng Recipes |
- Bảng ShoppingIngredients


    | Tên cột | Kiểu dữ liệu | Khóa/Ràng buộc | Mô tả |
    | --- | --- | --- | --- |
    | `siId` | `INT` | `PRIMARY KEY` | Mã định danh duy nhất |
    | `stId` | `INT` | `FOREIGN KEY REFERENCES ShoppingTimes(stId)` | liên kết đến bảng ShoppingTimes |
    | `ingredientId` | `INT` | `FOREIGN KEY REFERENCES Ingredients(ingredientId)`  | liên kết đến bảng Ingredients |
    | `weight` | `DOUBLE` | **`NOT NULL`** | Khối lượng |
    | `unit` | `NVARCHAR(10)` | `NOT NULL` | đơn vị |
    | `isBought` | `BOOLEAN` | `NOT NULL` | trạng thái đã mua |
- Bảng Steps


    | Tên cột | Kiểu dữ liệu | Khóa/Ràng buộc | Mô tả |
    | --- | --- | --- | --- |
    | `stepId` | `INT` | `PRIMARY KEY` | Mã định danh duy nhất |
    | `recipeId` | `INT` | `FOREIGN KEY REFERENCES Recipes(recipeId)` | liên kết đến bảng Recipes |
    | `index` | `INT` | `NOT NULL` | STT bước |
    | `content` | `NVARCHAR(500)` | `NOT NULL` | chi tiết bước |
