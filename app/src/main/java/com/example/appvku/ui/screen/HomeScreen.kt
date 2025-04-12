package com.example.appvku.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appvku.R
import com.example.appvku.model.MentorInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Dữ liệu tĩnh cho danh sách mentor
    val mentors = listOf(
        MentorInfo(
            id = "1",
            name = "Nguyen Quang Kinh",
            expertise = "Android, Kotlin",
            organization = "Tech Company",
            achievements = "5 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "2",
            name = "Tran Thi Bich",
            expertise = "iOS, Swift",
            organization = "Mobile Dev Corp",
            achievements = "3 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "3",
            name = "Le Van Cuong",
            expertise = "Flutter, Dart",
            organization = "Startup X",
            achievements = "4 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "4",
            name = "Pham Minh Duc",
            expertise = "React Native",
            organization = "Freelancer",
            achievements = "2 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "5",
            name = "Nguyen Thi Hoa",
            expertise = "Java, Spring",
            organization = "Big Tech",
            achievements = "6 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "6",
            name = "Hoang Van Nam",
            expertise = "Python, Django",
            organization = "Tech Startup",
            achievements = "3 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "7",
            name = "Vu Thi Lan",
            expertise = "UI/UX Design",
            organization = "Design Agency",
            achievements = "4 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "8",
            name = "Do Minh Tuan",
            expertise = "DevOps",
            organization = "Cloud Company",
            achievements = "5 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "9",
            name = "Nguyen Van Anh",
            expertise = "Machine Learning",
            organization = "AI Lab",
            achievements = "3 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "10",
            name = "Tran Van Bao",
            expertise = "Web Development",
            organization = "Web Agency",
            achievements = "4 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "11",
            name = "Le Thi Cam",
            expertise = "Cybersecurity",
            organization = "Security Firm",
            achievements = "5 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "12",
            name = "Pham Van Dat",
            expertise = "Blockchain",
            organization = "Crypto Startup",
            achievements = "3 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "13",
            name = "Nguyen Thi Em",
            expertise = "Game Development",
            organization = "Game Studio",
            achievements = "4 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "14",
            name = "Hoang Van Giang",
            expertise = "Database Admin",
            organization = "Data Corp",
            achievements = "6 years of experience",
            image = "nguyenquangkinh.jpg"
        ),
        MentorInfo(
            id = "15",
            name = "Vu Thi Hong",
            expertise = "QA Engineer",
            organization = "Testing Lab",
            achievements = "3 years of experience",
            image = "nguyenquangkinh.jpg"
        )
    )

    // State để quản lý danh sách mentor hiển thị
    var visibleMentors by remember { mutableStateOf(mentors.take(9)) } // Hiển thị 9 mentor đầu tiên
    var page by remember { mutableStateOf(0) }
    val mentorsPerPage = 9 // Số mentor hiển thị ban đầu
    val additionalMentors = 6 // Số mentor hiển thị thêm mỗi lần nhấn "Xem thêm"

    // State để theo dõi vị trí cuộn
    val gridState = rememberLazyGridState()

    // State để theo dõi mục được chọn trong NavigationBar
    val selectedItem by remember { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController) { scope.launch { drawerState.close() } }
        }
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE5F0FF))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Hàng 1: Menu icon (bên trái) và VKU Mentor (giữa)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "VKU Mentor",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Hàng 2: Các icon (làm nổi bật block)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp)) // Thêm bóng
                            .background(Color.White, RoundedCornerShape(8.dp)) // Background trắng, bo góc
                            .padding(8.dp), // Padding bên trong block
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { navController.navigate("register_mentor") }) {
                            Image(
                                painter = painterResource(id = R.drawable.register),
                                contentDescription = "Đăng ký Mentor",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { navController.navigate("search_mentor") }) {
                            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm Mentor")
                        }
                        IconButton(onClick = { navController.navigate("community") }) {
                            Image(
                                painter = painterResource(id = R.drawable.group),
                                contentDescription = "Cộng đồng",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { navController.navigate("collaboration") }) {
                            Image(
                                painter = painterResource(id = R.drawable.hoptac),
                                contentDescription = "Hợp tác",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { navController.navigate("rating") }) {
                            Image(
                                painter = painterResource(id = R.drawable.rating),
                                contentDescription = "Đánh giá",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = { navController.navigate("about_us") }) {
                            Image(
                                painter = painterResource(id = R.drawable.vechungto),
                                contentDescription = "Về chúng tớ",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                // Sử dụng NavigationBar thay vì BottomNavigation
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE5F0FF))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp)) // Thêm bóng
                        .background(Color.White, RoundedCornerShape(8.dp)) // Background trắng, bo góc
                        .padding(8.dp), // Padding bên trong block
                ) {
                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.trangchu),
                                contentDescription = "Trang chủ",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Trang chủ",
                                fontSize = 12.sp,
                                color = if (selectedItem == "home") Color.Black else Color.Gray
                            )
                        },
                        selected = selectedItem == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.thongbao),
                                contentDescription = "Thông báo",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Thông báo",
                                fontSize = 12.sp,
                                color = if (selectedItem == "notifications") Color.Black else Color.Gray
                            )
                        },
                        selected = selectedItem == "notifications",
                        onClick = {
                            navController.navigate("notifications") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )

                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.taikhoan),
                                contentDescription = "Tài khoản",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Tài khoản",
                                fontSize = 12.sp,
                                color = if (selectedItem == "profile") Color.Black else Color.Gray
                            )
                        },
                        selected = selectedItem == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE5F0FF))
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Phần "Mentor nổi bật"
                        Text(
                            text = "Mentor nổi bật",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        // Danh sách mentor
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Hiển thị các mentor
                            items(visibleMentors) { mentor ->
                                MentorCard(mentor)
                            }

                            // Thêm một item cuối cùng chứa nút "Xem thêm" và "Ẩn"
                            item(span = { GridItemSpan(3) }) { // Span toàn bộ chiều rộng (3 cột)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Nút "Ẩn" (chỉ hiển thị nếu đã bấm "Xem thêm" và có thêm mentor)
                                    if (visibleMentors.size > mentorsPerPage) {
                                        Button(
                                            onClick = {
                                                visibleMentors = mentors.take(mentorsPerPage) // Reset về 9 mentor đầu
                                                page = 0
                                                scope.launch {
                                                    gridState.scrollToItem(0) // Cuộn lên đầu
                                                }
                                            },
                                            modifier = Modifier
                                                .width(120.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2961B4)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "Ẩn",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(120.dp)) // Giữ khoảng trống để căn chỉnh
                                    }

                                    // Nút "Xem thêm" (chỉ hiển thị nếu còn dữ liệu)
                                    if (visibleMentors.size < mentors.size) {
                                        Button(
                                            onClick = {
                                                page++
                                                val newVisibleCount = mentorsPerPage + (page * additionalMentors)
                                                visibleMentors = mentors.take(newVisibleCount)
                                            },
                                            modifier = Modifier
                                                .width(120.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "Xem thêm",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(120.dp)) // Giữ khoảng trống để căn chỉnh
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun DrawerContent(navController: NavHostController, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Trang chủ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("home")
                onClose()
            }
        )
        Text(
            text = "Đăng ký Mentor",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("register_mentor")
                onClose()
            }
        )
        Text(
            text = "Tìm kiếm Mentor",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("search_mentor")
                onClose()
            }
        )
        Text(
            text = "Cộng đồng",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("community")
                onClose()
            }
        )
        Text(
            text = "Hợp tác",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("collaboration")
                onClose()
            }
        )
        Text(
            text = "Đánh giá",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("rating")
                onClose()
            }
        )
        Text(
            text = "Về chúng tớ",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                navController.navigate("about_us")
                onClose()
            }
        )
    }
}

@Composable
fun MentorCard(mentor: MentorInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp) // Đặt chiều cao cố định để các card đều nhau
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Đảm bảo Column chiếm toàn bộ không gian của Card
                .background(Color.White)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Phân bổ đều các thành phần
        ) {
            // Hình ảnh mentor (không bo tròn)
            Image(
                painter = painterResource(id = R.drawable.nguyenquangkinh),
                contentDescription = "Mentor Image",
                modifier = Modifier
                    .size(100.dp)
            )

            // Họ và tên
            Text(
                text = mentor.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1, // Giới hạn 1 dòng
                overflow = TextOverflow.Ellipsis, // Cắt bớt nếu quá dài
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 16.sp)
            )

            // Chuyên môn
            Text(
                text = mentor.expertise ?: "Chưa có thông tin",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1, // Giới hạn 1 dòng
                overflow = TextOverflow.Ellipsis, // Cắt bớt nếu quá dài
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 14.sp)
            )

            // Tổ chức
            Text(
                text = mentor.organization ?: "Chưa có thông tin",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1, // Giới hạn 1 dòng
                overflow = TextOverflow.Ellipsis, // Cắt bớt nếu quá dài
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 14.sp)
            )

            // Thành tựu
            Text(
                text = mentor.achievements ?: "Chưa có thông tin",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1, // Giới hạn 1 dòng
                overflow = TextOverflow.Ellipsis, // Cắt bớt nếu quá dài
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 14.sp)
            )
        }
    }
}