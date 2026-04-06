# ChefMate Android

[English](README.md)

ChefMate là ứng dụng đồng hành nấu ăn trên Android được xây dựng bằng Kotlin và Jetpack Compose. Ứng dụng kết hợp khám phá công thức, lập danh sách mua sắm, lên kế hoạch theo nguyên liệu sẵn có và `Bepes`, trợ lý nấu ăn theo phiên giúp người dùng sắp xếp và nấu một hoặc nhiều món trong cùng một bữa.

Repository này chỉ chứa Android client.

## Ứng Dụng Làm Gì

- Duyệt và tìm kiếm công thức nấu ăn
- Xem công thức trending và gợi ý cá nhân hóa
- Lưu, tạo, chỉnh sửa và quản lý công thức
- Tạo danh sách mua sắm từ công thức và xem lịch sử mua sắm
- Quản lý nguyên liệu sẵn có và ghi chú ăn uống
- Đăng nhập, đăng ký và chỉnh sửa hồ sơ
- Trò chuyện với `Bepes` để lên kế hoạch và nấu ăn theo ngữ cảnh phiên hiện tại

## Các Khu Vực Chính Trong App

- `Auth`: đăng nhập, đăng ký, chỉnh sửa hồ sơ
- `Home`: khám phá nội dung, gợi ý và các điểm vào luồng nấu ăn
- `Recipes`: danh sách, tìm kiếm, xem chi tiết, tạo mới và quản lý kho công thức cá nhân
- `Shopping`: tạo danh sách nguyên liệu và xem lịch sử mua sắm
- `Pantry & Diet Notes`: quản lý nguyên liệu có sẵn và ghi chú ăn uống
- `Bepes Chat`: phiên nấu ăn được điều khiển bởi server với món ưu tiên, ngữ cảnh bữa ăn và lịch sử chat

## Công Nghệ Sử Dụng

- Kotlin
- Jetpack Compose
- Kiến trúc MVVM
- Room
- OkHttp
- Gson
- Coil
- DataStore
- Android Security Crypto

## Thiết Lập Môi Trường Local

### Yêu Cầu

- Android Studio
- JDK 11
- Android SDK với API level `35`
- Một môi trường backend ChefMate đang chạy

### Cấu Hình `local.properties`

Sao chép file mẫu rồi điền giá trị local của bạn:

```bash
cp local.properties.example local.properties
```

Các key bắt buộc:

```properties
CHEFMATE_API_BASE_URL=https://your-api-host.example.com
CHEFMATE_CHAT_API_KEY=replace-with-chat-api-key
```

Android build sẽ đọc các giá trị này trong `app/build.gradle.kts` và fail ngay nếu thiếu hoặc rỗng.

Bạn cũng có thể thêm cấu hình signing local trong `local.properties`:

```properties
KEY_ALIAS=projectkey
KEYSTORE_PASSWORD=
KEY_PASSWORD=
```

## Build Và Chạy App

Cài bản debug lên thiết bị hoặc emulator đang kết nối:

```bash
./gradlew :app:installDebug
```

Mở app bằng `adb`:

```bash
adb shell am start -n com.watb.chefmate/.ui.main.MainActivity
```

Nếu chỉ muốn build mà chưa cài:

```bash
./gradlew :app:assembleDebug
```

## Ghi Chú Kiến Trúc

Đây là Android client giao tiếp với backend riêng. App dùng kiến trúc MVVM trên Jetpack Compose, có local persistence cho dữ liệu ứng dụng và secure storage cho session nhạy cảm.

`Bepes` là luồng trợ lý nấu ăn có AI trong app. Client hiện tại dùng mô hình chat/session do server điều khiển, trong đó backend quản lý trạng thái phiên nấu, lịch sử tin nhắn, món đang ưu tiên và tiến độ nấu, còn Android client chịu trách nhiệm hiển thị và tương tác với trạng thái đó.

## Cấu Trúc Repository

- `app/`: mã nguồn Android application
- `gradle/`: Gradle version catalog và wrapper configuration
- `local.properties.example`: mẫu cấu hình local cho các API key bắt buộc

## Hệ Sinh Thái Dự Án

- Server: [ChefMate_Server](https://github.com/PhongDayNai/ChefMate_Server)
- Web client: [Chefmate_Web_Client](https://github.com/PhongDayNai/Chefmate_Web_Client)
- Admin web: [ChefMate_Admin_Web](https://github.com/PhongDayNai/ChefMate_Admin_Web)

## Trạng Thái Open Source

Repository này đang được dọn lại và viết tài liệu theo hướng public/open-source. Nếu bạn gặp lỗi, muốn đề xuất cải tiến hoặc muốn đóng góp bản sửa, bạn có thể mở issue hoặc pull request.

Một số tài liệu cộng đồng như license hoặc contribution guide có thể sẽ được bổ sung riêng sau.
