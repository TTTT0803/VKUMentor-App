package com.example.appvku

import android.util.Log
import com.example.appvku.model.*
import kotlinx.coroutines.tasks.await

object DatabaseInitializer {

    private const val TAG = "DatabaseInitializer"

    suspend fun initializeDatabase() {
        try {
            // 1. Khởi tạo collection "roles"
            val rolesCollection = FirestoreClient.db.collection(FirestoreClient.ROLES_COLLECTION)
            if (isCollectionEmpty(FirestoreClient.ROLES_COLLECTION)) {
                Log.d(TAG, "Khởi tạo collection roles...")
                val roleMentor = Role(roleName = "Mentor")
                val roleMentee = Role(roleName = "Mentee")
                val roleAdmin = Role(roleName = "Admin")

                val mentorRoleDoc = rolesCollection.add(roleMentor).await()
                Log.d(TAG, "Đã thêm vai trò Mentor với ID: ${mentorRoleDoc.id}")
                val menteeRoleDoc = rolesCollection.add(roleMentee).await()
                Log.d(TAG, "Đã thêm vai trò Mentee với ID: ${menteeRoleDoc.id}")
                val adminRoleDoc = rolesCollection.add(roleAdmin).await()
                Log.d(TAG, "Đã thêm vai trò Admin với ID: ${adminRoleDoc.id}")

                // 2. Khởi tạo collection "users"
                val usersCollection = FirestoreClient.db.collection(FirestoreClient.USERS_COLLECTION)
                val mentorUser = User(
                    username = "mentor1",
                    password = "hashed_password_1",
                    email = "mentor1@example.com",
                    idRole = mentorRoleDoc.id
                )
                val menteeUser = User(
                    username = "mentee1",
                    password = "hashed_password_2",
                    email = "mentee1@example.com",
                    idRole = menteeRoleDoc.id
                )

                val mentorUserDoc = usersCollection.add(mentorUser).await()
                Log.d(TAG, "Đã thêm người dùng Mentor với ID: ${mentorUserDoc.id}")
                val menteeUserDoc = usersCollection.add(menteeUser).await()
                Log.d(TAG, "Đã thêm người dùng Mentee với ID: ${menteeUserDoc.id}")

                // 3. Khởi tạo collection "mentor_info"
                val mentorInfoCollection = FirestoreClient.db.collection(FirestoreClient.MENTOR_INFO_COLLECTION)
                val mentorInfo = MentorInfo(
                    name = "Nguyen Van A",
                    achievements = "5 năm kinh nghiệm trong lập trình Android",
                    expertise = "Android, Kotlin",
                    organization = "Tech Company",
                    referralSource = "Bạn bè",
                    status = "approved", // Thêm trạng thái mặc định
                    suggestionsQuestions = "Làm thế nào để cải thiện kỹ năng lập trình?",
                    image = "https://example.com/mentor_image.jpg",
                    userId = mentorUserDoc.id
                )
                val mentorInfoDoc = mentorInfoCollection.add(mentorInfo).await()
                Log.d(TAG, "Đã thêm MentorInfo với ID: ${mentorInfoDoc.id}")

                // 4. Khởi tạo collection "competitions"
                val competitionsCollection = FirestoreClient.db.collection(FirestoreClient.COMPETITIONS_COLLECTION)
                val competition = Competition(
                    name = "Thử thách lập trình 2025",
                    description = "Một cuộc thi dành cho các lập trình viên trẻ",
                    date = "2025-04-10",
                    image = "https://example.com/competition_image.jpg"
                )
                val competitionDoc = competitionsCollection.add(competition).await()
                Log.d(TAG, "Đã thêm Competition với ID: ${competitionDoc.id}")

                // 5. Khởi tạo collection "mentor_mentee_list"
                val mentorMenteeListCollection = FirestoreClient.db.collection(FirestoreClient.MENTOR_MENTEE_LIST_COLLECTION)
                val mentorMenteeList = MentorMenteeList(
                    mentorId = mentorUserDoc.id,
                    menteeId = menteeUserDoc.id
                )
                val mentorMenteeListDoc = mentorMenteeListCollection.add(mentorMenteeList).await()
                Log.d(TAG, "Đã thêm MentorMenteeList với ID: ${mentorMenteeListDoc.id}")

                // 6. Khởi tạo collection "competitions_register"
                val competitionsRegisterCollection = FirestoreClient.db.collection(FirestoreClient.COMPETITIONS_REGISTER_COLLECTION)
                val competitionRegister = CompetitionsRegister(
                    userId = menteeUserDoc.id,
                    competitionId = competitionDoc.id,
                    registrationDate = "2025-04-05"
                )
                val competitionRegisterDoc = competitionsRegisterCollection.add(competitionRegister).await()
                Log.d(TAG, "Đã thêm CompetitionsRegister với ID: ${competitionRegisterDoc.id}")

                // 7. Khởi tạo collection "mentor_rating"
                val mentorRatingCollection = FirestoreClient.db.collection(FirestoreClient.MENTOR_RATING_COLLECTION)
                val mentorRating = MentorRating(
                    mentorId = mentorInfoDoc.id,
                    userId = menteeUserDoc.id,
                    rating = 5,
                    comment = "Mentor tuyệt vời!",
                    date = "2025-04-05"
                )
                val mentorRatingDoc = mentorRatingCollection.add(mentorRating).await()
                Log.d(TAG, "Đã thêm MentorRating với ID: ${mentorRatingDoc.id}")

                // 8. Khởi tạo collection "university_partners"
                val universityPartnersCollection = FirestoreClient.db.collection(FirestoreClient.UNIVERSITY_PARTNERS_COLLECTION)
                val universityPartner = UniversityPartner(
                    name = "Đại học Hà Nội",
                    location = "Hà Nội, Việt Nam",
                    description = "Một trường đại học hàng đầu về công nghệ",
                    image = "https://example.com/university_image.jpg"
                )
                val universityPartnerDoc = universityPartnersCollection.add(universityPartner).await()
                Log.d(TAG, "Đã thêm UniversityPartner với ID: ${universityPartnerDoc.id}")

                // 9. Khởi tạo collection "community_documents"
                val communityDocumentsCollection = FirestoreClient.db.collection(FirestoreClient.COMMUNITY_DOCUMENTS_COLLECTION)
                val communityDocument = CommunityDocument(
                    title = "Hướng dẫn lập trình Kotlin",
                    content = "Đây là hướng dẫn dành cho người mới bắt đầu học lập trình Kotlin.",
                    date = "2025-04-05",
                    image = "https://example.com/document_image.jpg",
                    mentorId = mentorInfoDoc.id
                )
                val communityDocumentDoc = communityDocumentsCollection.add(communityDocument).await()
                Log.d(TAG, "Đã thêm CommunityDocument với ID: ${communityDocumentDoc.id}")

                // 10. Khởi tạo collection "comments"
                val commentsCollection = FirestoreClient.db.collection(FirestoreClient.COMMENTS_COLLECTION)
                val comment = Comment(
                    content = "Hướng dẫn rất hữu ích!",
                    userId = menteeUserDoc.id,
                    postId = communityDocumentDoc.id,
                    date = "2025-04-05"
                )
                val commentDoc = commentsCollection.add(comment).await()
                Log.d(TAG, "Đã thêm Comment với ID: ${commentDoc.id}")

                // 11. Khởi tạo collection "slider_images"
                val sliderImagesCollection = FirestoreClient.db.collection(FirestoreClient.SLIDER_IMAGES_COLLECTION)
                val sliderImage = SliderImage(
                    image = "https://example.com/slider_image.jpg",
                    caption = "Chào mừng đến với nền tảng của chúng tôi!",
                    link = "https://example.com"
                )
                val sliderImageDoc = sliderImagesCollection.add(sliderImage).await()
                Log.d(TAG, "Đã thêm SliderImage với ID: ${sliderImageDoc.id}")

                // 12. Khởi tạo collection "admin"
                val adminCollection = FirestoreClient.db.collection(FirestoreClient.ADMIN_COLLECTION)
                val admin = Admin(
                    email = "admin@example.com",
                    password = "admin_hashed_password",
                    idRole = adminRoleDoc.id
                )
                val adminDoc = adminCollection.add(admin).await()
                Log.d(TAG, "Đã thêm Admin với ID: ${adminDoc.id}")

                // 13. Khởi tạo collection "mentee_info"
                val menteeInfoCollection = FirestoreClient.db.collection(FirestoreClient.MENTEE_INFO_COLLECTION)
                val menteeInfo = MenteeInfo(
                    name = "Tran Thi B",
                    email = "mentee1@example.com",
                    mentorId = mentorInfoDoc.id
                )
                val menteeInfoDoc = menteeInfoCollection.add(menteeInfo).await()
                Log.d(TAG, "Đã thêm MenteeInfo với ID: ${menteeInfoDoc.id}")

                // 14. Khởi tạo collection "contact_messages"
                val contactMessagesCollection = FirestoreClient.db.collection(FirestoreClient.CONTACT_MESSAGES_COLLECTION)
                val contactMessage = ContactMessage(
                    name = "Nguyen Van C",
                    email = "user@example.com",
                    message = "Tôi cần hỗ trợ với tài khoản của mình.",
                    date = "2025-04-05"
                )
                val contactMessageDoc = contactMessagesCollection.add(contactMessage).await()
                Log.d(TAG, "Đã thêm ContactMessage với ID: ${contactMessageDoc.id}")

                Log.d(TAG, "Khởi tạo cơ sở dữ liệu thành công!")
            } else {
                Log.d(TAG, "Cơ sở dữ liệu đã được khởi tạo trước đó!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi khởi tạo cơ sở dữ liệu: ${e.message}", e)
            throw e
        }
    }

    private suspend fun isCollectionEmpty(collectionName: String): Boolean {
        try {
            val snapshot = FirestoreClient.db.collection(collectionName).get().await()
            Log.d(TAG, "Collection $collectionName có ${snapshot.size()} tài liệu")
            return snapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi kiểm tra collection $collectionName có rỗng không: ${e.message}", e)
            throw e
        }
    }
}