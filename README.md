# Hệ thống ứng dụng công thức nấu ăn - ChefMate

# I. Client - Ứng dụng di động

## 1. Công nghệ

- **Jetpack Compose**: Framework UI động.
- **Room Database**: Lưu trữ dữ liệu cục bộ, trừu tượng hóa SQLite.
- **OkHttp**: Gọi API RESTful đến server.
- **Coil**: Tải và hiển thị hình ảnh từ URL.
- **Gson**: Chuyển đổi Java thành JSON và ngược lại.

## 2. Kiến trúc: Mô hình MVVM

- **Model (M)**: Chứa các lớp dữ liệu và logic tương tác với kho dữ liệu.
    - **RoomDB Entities**: Định nghĩa cấu trúc dữ liệu cục bộ.
    - **DAOs**: Cung cấp phương thức tương tác với RoomDB.
    - **Repositories**: Lớp trung gian truy cập dữ liệu, ẩn nguồn (RoomDB hoặc API Server).
- **View (V)**: Thành phần giao diện người dùng, hiển thị dữ liệu và nhận tương tác.
    - **Composables (Jetpack Compose)**: Hàm xây dựng giao diện động.
    - **Hiển thị dữ liệu**: Quan sát dữ liệu từ ViewModel và cập nhật giao diện.
    - **Xử lý sự kiện**: Gửi sự kiện người dùng (click, nhập liệu) đến ViewModelScope.
- **ViewModel (VM)**: Xử lý logic nghiệp vụ, quản lý trạng thái giao diện và tương tác với Model.
    - **Logic nghiệp vụ**: Thêm, sửa, xóa, tìm kiếm công thức, quản lý danh sách mua sắm.
    - **Cung cấp dữ liệu**: Sử dụng Flow để tự động cập nhật dữ liệu cho View.
    - **Tương tác với Repository**: Gọi phương thức từ Repository để lấy hoặc lưu dữ liệu.

## 3. Tính năng

| User | Recipe | Interaction | Khác |
| --- | --- | --- | --- |
| Đăng nhập | Hiển thị công thức nấu ăn | Yêu thích | Kiểm tra kết nối internet |
| Đăng ký | Tìm kiếm công thức theo tên | Bình luận |  |
| Đổi mật khẩu | Tìm kiếm công thức theo tag | Chia sẻ |  |
| Đăng xuất | Lưu vào “Kho công thức” |  |  |
| Chỉnh sửa thông tin cá nhân | Lập danh sách mua sắm |  |  |
|  | Quản lý danh sách mua sắm |  |  |
|  | Tạo công thức mới |  |  |
|  | Chỉnh sửa công thức trong “Kho công thức” |  |  |
|  | Xóa khỏi “Kho công thức” |  |  |
|  | Lịch sử đăng công thức |  |  |
|  | Lịch sử mua sắm |  |  |
|  | Lấy danh sách công thức Top Trending |  |  |
|  | Lấy danh sách nguyên liệu sẵn có |  |  |
|  | Lấy danh sách tag sẵn có |  |  |

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

### d. Khác

- [x]  Kiểm tra kết nối Internet

## 4. Cơ sở dữ liệu

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

# [II. Server - Máy chủ](https://github.com/PhongDayNai/ChefMate_Server)
# [III. Client - Admin Web](https://github.com/PhongDayNai/ChefMate_Admin_Web)