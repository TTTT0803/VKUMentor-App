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
                Log.d(TAG, "Initializing roles collection...")
                val roleMentor = Role(roleName = "Mentor")
                val roleMentee = Role(roleName = "Mentee")
                val roleAdmin = Role(roleName = "Admin")

                val mentorRoleDoc = rolesCollection.add(roleMentor).await()
                Log.d(TAG, "Added Mentor role with ID: ${mentorRoleDoc.id}")
                val menteeRoleDoc = rolesCollection.add(roleMentee).await()
                Log.d(TAG, "Added Mentee role with ID: ${menteeRoleDoc.id}")
                val adminRoleDoc = rolesCollection.add(roleAdmin).await()
                Log.d(TAG, "Added Admin role with ID: ${adminRoleDoc.id}")

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
                Log.d(TAG, "Added Mentor user with ID: ${mentorUserDoc.id}")
                val menteeUserDoc = usersCollection.add(menteeUser).await()
                Log.d(TAG, "Added Mentee user with ID: ${menteeUserDoc.id}")

                // 3. Khởi tạo collection "mentor_info"
                val mentorInfoCollection = FirestoreClient.db.collection(FirestoreClient.MENTOR_INFO_COLLECTION)
                val mentorInfo = MentorInfo(
                    name = "Nguyen Van A",
                    achievements = "5 years of experience in Android Development",
                    expertise = "Android, Kotlin",
                    organization = "Tech Company",
                    referralSource = "Friend",
                    status = "Active",
                    suggestionsQuestions = "How to improve coding skills?",
                    image = "https://example.com/mentor_image.jpg",
                    userId = mentorUserDoc.id
                )
                val mentorInfoDoc = mentorInfoCollection.add(mentorInfo).await()
                Log.d(TAG, "Added MentorInfo with ID: ${mentorInfoDoc.id}")

                // 4. Khởi tạo collection "competitions"
                val competitionsCollection = FirestoreClient.db.collection(FirestoreClient.COMPETITIONS_COLLECTION)
                val competition = Competition(
                    name = "Coding Challenge 2025",
                    description = "A competition for young developers",
                    date = "2025-04-10",
                    image = "https://example.com/competition_image.jpg"
                )
                val competitionDoc = competitionsCollection.add(competition).await()
                Log.d(TAG, "Added Competition with ID: ${competitionDoc.id}")

                // 5. Khởi tạo collection "mentor_mentee_list"
                val mentorMenteeListCollection = FirestoreClient.db.collection(FirestoreClient.MENTOR_MENTEE_LIST_COLLECTION)
                val mentorMenteeList = MentorMenteeList(
                    mentorId = mentorUserDoc.id,
                    menteeId = menteeUserDoc.id
                )
                val mentorMenteeListDoc = mentorMenteeListCollection.add(mentorMenteeList).await()
                Log.d(TAG, "Added MentorMenteeList with ID: ${mentorMenteeListDoc.id}")

                // 6. Khởi tạo collection "competitions_register"
                val competitionsRegisterCollection = FirestoreClient.db.collection(FirestoreClient.COMPETITIONS_REGISTER_COLLECTION)
                val competitionRegister = CompetitionsRegister(
                    userId = menteeUserDoc.id,
                    competitionId = competitionDoc.id,
                    registrationDate = "2025-04-05"
                )
                val competitionRegisterDoc = competitionsRegisterCollection.add(competitionRegister).await()
                Log.d(TAG, "Added CompetitionsRegister with ID: ${competitionRegisterDoc.id}")

                // 7. Khởi tạo collection "mentor_rating"
                val mentorRatingCollection = FirestoreClient.db.collection(FirestoreClient.MENTOR_RATING_COLLECTION)
                val mentorRating = MentorRating(
                    mentorId = mentorInfoDoc.id,
                    userId = menteeUserDoc.id,
                    rating = 5,
                    comment = "Great mentor!",
                    date = "2025-04-05"
                )
                val mentorRatingDoc = mentorRatingCollection.add(mentorRating).await()
                Log.d(TAG, "Added MentorRating with ID: ${mentorRatingDoc.id}")

                // 8. Khởi tạo collection "university_partners"
                val universityPartnersCollection = FirestoreClient.db.collection(FirestoreClient.UNIVERSITY_PARTNERS_COLLECTION)
                val universityPartner = UniversityPartner(
                    name = "Hanoi University",
                    location = "Hanoi, Vietnam",
                    description = "A leading university in technology",
                    image = "https://example.com/university_image.jpg"
                )
                val universityPartnerDoc = universityPartnersCollection.add(universityPartner).await()
                Log.d(TAG, "Added UniversityPartner with ID: ${universityPartnerDoc.id}")

                // 9. Khởi tạo collection "community_documents"
                val communityDocumentsCollection = FirestoreClient.db.collection(FirestoreClient.COMMUNITY_DOCUMENTS_COLLECTION)
                val communityDocument = CommunityDocument(
                    title = "Guide to Kotlin",
                    content = "This is a guide for beginners in Kotlin programming.",
                    date = "2025-04-05",
                    image = "https://example.com/document_image.jpg",
                    mentorId = mentorInfoDoc.id
                )
                val communityDocumentDoc = communityDocumentsCollection.add(communityDocument).await()
                Log.d(TAG, "Added CommunityDocument with ID: ${communityDocumentDoc.id}")

                // 10. Khởi tạo collection "comments"
                val commentsCollection = FirestoreClient.db.collection(FirestoreClient.COMMENTS_COLLECTION)
                val comment = Comment(
                    content = "Very helpful guide!",
                    userId = menteeUserDoc.id,
                    postId = communityDocumentDoc.id,
                    date = "2025-04-05"
                )
                val commentDoc = commentsCollection.add(comment).await()
                Log.d(TAG, "Added Comment with ID: ${commentDoc.id}")

                // 11. Khởi tạo collection "slider_images"
                val sliderImagesCollection = FirestoreClient.db.collection(FirestoreClient.SLIDER_IMAGES_COLLECTION)
                val sliderImage = SliderImage(
                    image = "https://example.com/slider_image.jpg",
                    caption = "Welcome to our platform!",
                    link = "https://example.com"
                )
                val sliderImageDoc = sliderImagesCollection.add(sliderImage).await()
                Log.d(TAG, "Added SliderImage with ID: ${sliderImageDoc.id}")

                // 12. Khởi tạo collection "admin"
                val adminCollection = FirestoreClient.db.collection(FirestoreClient.ADMIN_COLLECTION)
                val admin = Admin(
                    email = "admin@example.com",
                    password = "admin_hashed_password",
                    idRole = adminRoleDoc.id
                )
                val adminDoc = adminCollection.add(admin).await()
                Log.d(TAG, "Added Admin with ID: ${adminDoc.id}")

                // 13. Khởi tạo collection "mentee_info"
                val menteeInfoCollection = FirestoreClient.db.collection(FirestoreClient.MENTEE_INFO_COLLECTION)
                val menteeInfo = MenteeInfo(
                    name = "Tran Thi B",
                    email = "mentee1@example.com",
                    mentorId = mentorInfoDoc.id
                )
                val menteeInfoDoc = menteeInfoCollection.add(menteeInfo).await()
                Log.d(TAG, "Added MenteeInfo with ID: ${menteeInfoDoc.id}")

                // 14. Khởi tạo collection "contact_messages"
                val contactMessagesCollection = FirestoreClient.db.collection(FirestoreClient.CONTACT_MESSAGES_COLLECTION)
                val contactMessage = ContactMessage(
                    name = "Nguyen Van C",
                    email = "user@example.com",
                    message = "I need help with my account.",
                    date = "2025-04-05"
                )
                val contactMessageDoc = contactMessagesCollection.add(contactMessage).await()
                Log.d(TAG, "Added ContactMessage with ID: ${contactMessageDoc.id}")

                Log.d(TAG, "Database initialized successfully!")
            } else {
                Log.d(TAG, "Database already initialized!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database: ${e.message}", e)
            throw e
        }
    }

    private suspend fun isCollectionEmpty(collectionName: String): Boolean {
        try {
            val snapshot = FirestoreClient.db.collection(collectionName).get().await()
            Log.d(TAG, "Collection $collectionName has ${snapshot.size()} documents")
            return snapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if collection $collectionName is empty: ${e.message}", e)
            throw e
        }
    }
}