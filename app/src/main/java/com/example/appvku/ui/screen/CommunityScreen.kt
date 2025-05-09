package com.example.appvku.ui.screen

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.appvku.LocalAuthState
import com.example.appvku.R
import com.example.appvku.model.CommunityDocument
import com.example.appvku.ui.component.AppBottomBar
import com.example.appvku.ui.component.AppTopBar
import com.example.appvku.ui.component.DrawerContent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) }

    var visiblePosts by remember { mutableStateOf(listOf<CommunityDocument>()) }
    var originalPosts by remember { mutableStateOf(listOf<CommunityDocument>()) }
    var lastDocument by remember { mutableStateOf<com.google.firebase.firestore.DocumentSnapshot?>(null) }
    var page by remember { mutableStateOf(0) }
    val postsPerPage = 9
    val additionalPosts = 6
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val gridState = rememberLazyGridState()
    val selectedItem by remember { mutableStateOf("community") }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var fileUrl by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }

    val authState = LocalAuthState.current
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fileUri = uri
        fileName = uri?.lastPathSegment ?: "Chưa có file được đính kèm"
        if (uri != null) {
            MediaManager.get().upload(uri)
                .option("resource_type", "raw") // Specify raw for non-image files like PDF
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("CommunityScreen", "Bắt đầu upload PDF lên Cloudinary")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        Log.d("CommunityScreen", "Đang upload PDF: $bytes/$totalBytes")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        fileUrl = resultData["secure_url"]?.toString() ?: resultData["url"]?.toString()
                        Log.d("CommunityScreen", "Upload PDF thành công, URL: $fileUrl")
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("CommunityScreen", "Lỗi khi upload PDF: ${error.description}")
                        fileUrl = null
                        Toast.makeText(context, "Lỗi khi upload PDF: ${error.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.d("CommunityScreen", "Đang thử upload lại PDF: ${error.description}")
                    }
                })
                .dispatch()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            MediaManager.get().upload(uri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("CommunityScreen", "Bắt đầu upload ảnh lên Cloudinary")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        Log.d("CommunityScreen", "Đang upload ảnh: $bytes/$totalBytes")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        imageUrl = resultData["secure_url"]?.toString() ?: resultData["url"]?.toString()
                        Log.d("CommunityScreen", "Upload ảnh thành công, URL: $imageUrl")
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("CommunityScreen", "Lỗi khi upload ảnh: ${error.description}")
                        imageUrl = null
                        Toast.makeText(context, "Lỗi khi upload ảnh: ${error.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.d("CommunityScreen", "Đang thử upload lại ảnh: ${error.description}")
                    }
                })
                .dispatch()
        }
    }

    fun loadPosts() {
        isLoading = true
        errorMessage = null
        db.collection("community_documents")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(postsPerPage.toLong())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("CommunityScreen", "Không tìm thấy tài liệu nào trong community_documents")
                    errorMessage = "Không có bài đăng nào để hiển thị."
                } else {
                    val fetchedPosts = documents.map { doc ->
                        val post = doc.toObject(CommunityDocument::class.java)
                        post.copy(id = doc.id)
                    }
                    Log.d("CommunityScreen", "Tải được ${fetchedPosts.size} bài đăng: $fetchedPosts")
                    visiblePosts = fetchedPosts
                    originalPosts = fetchedPosts
                    lastDocument = documents.documents.lastOrNull()
                }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("CommunityScreen", "Lỗi khi tải bài đăng: ${exception.message}", exception)
                errorMessage = "Lỗi khi tải dữ liệu: ${exception.message}"
                isLoading = false
            }
    }

    fun searchPosts(query: String) {
        searchQuery = query
        if (query.isBlank()) {
            visiblePosts = originalPosts
        } else {
            visiblePosts = originalPosts.filter {
                it.title.lowercase().contains(query.lowercase())
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPosts()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController) { scope.launch { drawerState.close() } }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AppTopBar(
                    navController = navController,
                    drawerState = drawerState,
                    userRole = authState.userRole,
                    isRoleLoading = authState.isRoleLoading,
                    snackbarHostState = snackbarHostState
                )
            },
            bottomBar = {
                AppBottomBar(
                    navController = navController,
                    selectedItem = selectedItem
                )
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cộng đồng",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            TextField(
                                value = searchQuery,
                                onValueChange = { newQuery ->
                                    searchQuery = newQuery
                                    searchPosts(newQuery)
                                },
                                label = { Text("Tìm theo tiêu đề") },
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(48.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Black,
                                    unfocusedIndicatorColor = Color.Black,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedLabelColor = Color.Black,
                                    unfocusedLabelColor = Color.Black
                                )
                            )
                            if (authState.userRole?.lowercase() == "mentor") {
                                IconButton(
                                    onClick = {
                                        Log.d("CommunityScreen", "Nút thêm được nhấn, showBottomSheet = true")
                                        showBottomSheet = true
                                        currentStep = 1
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .shadow(4.dp, RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.add),
                                        contentDescription = "Thêm tài liệu",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (errorMessage != null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorMessage ?: "Đã có lỗi xảy ra",
                                    color = Color.Red,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(1),
                                state = gridState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(visiblePosts) { post ->
                                    PostCard(post, context)
                                }

                                item(span = { GridItemSpan(1) }) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (visiblePosts.size > postsPerPage) {
                                            Button(
                                                onClick = {
                                                    isLoading = true
                                                    val db = FirebaseFirestore.getInstance()
                                                    db.collection("community_documents")
                                                        .orderBy("date", Query.Direction.DESCENDING)
                                                        .limit(postsPerPage.toLong())
                                                        .get()
                                                        .addOnSuccessListener { documents ->
                                                            val fetchedPosts = documents.map { doc ->
                                                                val post = doc.toObject(CommunityDocument::class.java)
                                                                post.copy(id = doc.id)
                                                            }
                                                            visiblePosts = fetchedPosts
                                                            originalPosts = fetchedPosts
                                                            lastDocument = documents.documents.lastOrNull()
                                                            page = 0
                                                            scope.launch {
                                                                gridState.scrollToItem(0)
                                                            }
                                                            isLoading = false
                                                        }
                                                        .addOnFailureListener {
                                                            isLoading = false
                                                        }
                                                },
                                                modifier = Modifier.width(120.dp),
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
                                            Spacer(modifier = Modifier.width(120.dp))
                                        }

                                        if (lastDocument != null) {
                                            Button(
                                                onClick = {
                                                    isLoading = true
                                                    val db = FirebaseFirestore.getInstance()
                                                    db.collection("community_documents")
                                                        .orderBy("date", Query.Direction.DESCENDING)
                                                        .startAfter(lastDocument)
                                                        .limit(additionalPosts.toLong())
                                                        .get()
                                                        .addOnSuccessListener { documents ->
                                                            if (documents.isEmpty) {
                                                                lastDocument = null
                                                            } else {
                                                                val newPosts = documents.map { doc ->
                                                                    val post = doc.toObject(CommunityDocument::class.java)
                                                                    post.copy(id = doc.id)
                                                                }
                                                                visiblePosts = visiblePosts + newPosts
                                                                originalPosts = originalPosts + newPosts
                                                                lastDocument = documents.documents.lastOrNull()
                                                                page++
                                                            }
                                                            isLoading = false
                                                        }
                                                        .addOnFailureListener {
                                                            isLoading = false
                                                        }
                                                },
                                                modifier = Modifier.width(120.dp),
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
                                            Spacer(modifier = Modifier.width(120.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                Log.d("CommunityScreen", "Bottom sheet bị đóng, showBottomSheet = false")
                                showBottomSheet = false
                                currentStep = 1
                                title = ""
                                content = ""
                                fileUri = null
                                fileName = ""
                                fileUrl = null
                                imageUri = null
                                imageUrl = null
                            },
                            sheetState = sheetState
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .background(Color.White),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "VKU Alumnimentor",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = "Chia sẻ tài liệu của bạn và ngành ngay 2 tuần Premium miễn phí! Tài liệu upload là tài liệu có hội truy cập kho tài liệu đặc biệt không giới hạn!",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check_circle),
                                        contentDescription = null,
                                        tint = if (currentStep >= 1) Color.Green else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Divider(
                                        color = if (currentStep >= 1) Color.Green else Color.Gray,
                                        thickness = 2.dp,
                                        modifier = Modifier.width(50.dp)
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_circle),
                                        contentDescription = null,
                                        tint = if (currentStep >= 2) Color.Green else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Divider(
                                        color = if (currentStep >= 2) Color.Green else Color.Gray,
                                        thickness = 2.dp,
                                        modifier = Modifier.width(50.dp)
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_circle),
                                        contentDescription = null,
                                        tint = if (currentStep >= 3) Color.Green else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(
                                        "Nhập thông tin",
                                        fontSize = 12.sp,
                                        color = if (currentStep == 1) Color.Black else Color.Gray
                                    )
                                    Text(
                                        "Thêm hình ảnh",
                                        fontSize = 12.sp,
                                        color = if (currentStep == 2) Color.Black else Color.Gray
                                    )
                                    Text(
                                        "Xác nhận",
                                        fontSize = 12.sp,
                                        color = if (currentStep == 3) Color.Black else Color.Gray
                                    )
                                }

                                if (currentStep == 1) {
                                    Text(
                                        text = "Nhập thông tin tài liệu",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp, bottom = 8.dp)
                                    )

                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = { title = it },
                                        label = { Text("Tiêu đề") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        textStyle = TextStyle(fontSize = 14.sp)
                                    )

                                    OutlinedTextField(
                                        value = content,
                                        onValueChange = { content = it },
                                        label = { Text("Nội dung") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        textStyle = TextStyle(fontSize = 14.sp)
                                    )

                                    OutlinedTextField(
                                        value = fileName,
                                        onValueChange = { },
                                        label = { Text("File PDF đính kèm") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        textStyle = TextStyle(fontSize = 14.sp),
                                        enabled = false
                                    )

                                    Button(
                                        onClick = { filePickerLauncher.launch("application/pdf") }, // Restrict to PDF files
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "Chọn file PDF",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Text(
                                        text = if (fileName.isEmpty()) "Chưa có file PDF được đính kèm" else "File: $fileName",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = {
                                                Log.d("CommunityScreen", "Nút quay về được nhấn, showBottomSheet = false")
                                                showBottomSheet = false
                                                currentStep = 1
                                            },
                                            modifier = Modifier.width(120.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "Quay về",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                Log.d("CommunityScreen", "Nút tiếp tục được nhấn, chuyển sang bước 2")
                                                currentStep = 2
                                            },
                                            modifier = Modifier.width(120.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "Tiếp tục",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                if (currentStep == 2) {
                                    Text(
                                        text = "Thêm hình ảnh",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp, bottom = 8.dp)
                                    )

                                    if (imageUrl != null) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Hình ảnh từ Cloudinary",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Text(
                                            text = "Chưa có hình ảnh được chọn",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }

                                    Button(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "Chọn hình ảnh",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = {
                                                Log.d("CommunityScreen", "Nút quay về được nhấn, chuyển về bước 1")
                                                currentStep = 1
                                            },
                                            modifier = Modifier.width(120.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "Quay về",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                Log.d("CommunityScreen", "Nút tiếp tục được nhấn, chuyển sang bước 3")
                                                currentStep = 3
                                            },
                                            modifier = Modifier.width(120.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "Tiếp tục",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                if (currentStep == 3) {
                                    Text(
                                        text = "Xác nhận thông tin",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp, bottom = 8.dp)
                                    )

                                    Text(
                                        text = "Tiêu đề: $title",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    )

                                    Text(
                                        text = "Nội dung: ${content ?: "Không có nội dung"}",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    )

                                    Text(
                                        text = "File PDF: ${fileName.ifEmpty { "Không có file" }}",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    )

                                    if (imageUrl != null) {
                                        Text(
                                            text = "Hình ảnh:",
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        )
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = "Hình ảnh từ Cloudinary",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Text(
                                            text = "Không có hình ảnh",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp, top = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = {
                                                Log.d("CommunityScreen", "Nút quay về được nhấn, chuyển về bước 1")
                                                currentStep = 1
                                            },
                                            modifier = Modifier.width(120.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "Quay về",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (title.isNotEmpty() && imageUrl != null) {
                                                    val newPost = CommunityDocument(
                                                        id = db.collection("community_documents").document().id,
                                                        title = title,
                                                        content = content,
                                                        date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(Date()),
                                                        image = imageUrl,
                                                        mentorId = authState.currentUser?.uid ?: "Unknown",
                                                        fileUrl = fileUrl
                                                    )
                                                    Log.d("CommunityScreen", "Lưu bài đăng với URL ảnh: ${newPost.image}, URL PDF: ${newPost.fileUrl}")
                                                    db.collection("community_documents")
                                                        .document(newPost.id)
                                                        .set(newPost)
                                                        .addOnSuccessListener {
                                                            Log.d("CommunityScreen", "Lưu bài đăng thành công")
                                                            Toast.makeText(context, "Đăng bài thành công!", Toast.LENGTH_SHORT).show()
                                                            showBottomSheet = false
                                                            currentStep = 1
                                                            title = ""
                                                            content = ""
                                                            fileUri = null
                                                            fileName = ""
                                                            fileUrl = null
                                                            imageUri = null
                                                            imageUrl = null
                                                            loadPosts()
                                                        }
                                                        .addOnFailureListener { exception ->
                                                            Log.e("CommunityScreen", "Lỗi khi lưu bài đăng: ${exception.message}", exception)
                                                            Toast.makeText(context, "Lỗi khi đăng bài: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                } else {
                                                    Log.w("CommunityScreen", "Không thể xác nhận: Thiếu tiêu đề hoặc imageUrl")
                                                    Toast.makeText(context, "Vui lòng nhập tiêu đề và chọn hình ảnh!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.width(120.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(8.dp),
                                            enabled = title.isNotEmpty() && imageUrl != null
                                        ) {
                                            Text(
                                                "Xác nhận",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
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
fun PostCard(post: CommunityDocument, context: Context) {
    var mentorName by remember { mutableStateOf<String?>(null) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(post.mentorId) {
        if (post.mentorId != "Unknown") {
            db.collection("users")
                .document(post.mentorId)
                .get()
                .addOnSuccessListener { document ->
                    mentorName = document.getString("username") ?: "Người dùng ẩn danh"
                }
                .addOnFailureListener {
                    mentorName = "Người dùng ẩn danh"
                }
        } else {
            mentorName = "Người dùng ẩn danh"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                if (post.fileUrl != null) {
                    try {
                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val uri = Uri.parse(post.fileUrl)
                        val request = DownloadManager.Request(uri).apply {
                            setTitle("${post.title}.pdf")
                            setDescription("Downloading PDF from VKU Alumnimentor")
                            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${post.title}.pdf")
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                        }
                        downloadManager.enqueue(request)
                        Toast.makeText(context, "Đang tải ${post.title}.pdf...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("PostCard", "Lỗi khi tải PDF: ${e.message}", e)
                        Toast.makeText(context, "Lỗi khi tải PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Bài đăng này không có file PDF!", Toast.LENGTH_SHORT).show()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Log.d("PostCard", "Hình ảnh URL từ Firestore: ${post.image}")
            AsyncImage(
                model = post.image.takeIf { !it.isNullOrEmpty() && it.startsWith("https") } ?: R.drawable.post_image,
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                placeholder = painterResource(id = R.drawable.post_image),
                error = painterResource(id = R.drawable.post_image),
                onError = { Log.e("PostCard", "Lỗi tải hình ảnh: ${post.image}") }
            )

            Text(
                text = post.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 16.sp)
            )

            Text(
                text = post.content ?: "Không có nội dung",
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(lineHeight = 14.sp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = post.date.substring(0, 10),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(lineHeight = 12.sp)
                )
                Text(
                    text = mentorName ?: "Đang tải...",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(lineHeight = 12.sp)
                )
            }

            if (post.fileUrl != null) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dowload), // Ensure you have a download icon in res/drawable
                    contentDescription = "Tải PDF",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.End)
                )
            }
        }
    }
}