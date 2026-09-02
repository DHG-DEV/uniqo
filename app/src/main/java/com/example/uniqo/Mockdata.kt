package com.example.uniqo


/**
 * Realistic seed data so the UI is fully populated without a backend.
 * Swap MockRepository (data/repository) for a Supabase-backed implementation
 * later — screens only depend on the Repository interface, not this object.
 */
object MockData {

    val currentUser = Student(
        id = "u0",
        name = "Ankit Sharma",
        avatarUrl = "https://i.pravatar.cc/150?img=12",
        college = "ABC Engineering College",
        course = "Computer ",
        year = "2nd Year",
        verification = VerificationState(true, true, false),
        rating = 4.8,
        listingsCount = 28,
        soldCount = 12
    )

    private fun student(id: String, name: String, img: Int, college: String, course: String, year: String, verified: Boolean = true) =
        Student(
            id = id,
            name = name,
            avatarUrl = "https://i.pravatar.cc/150?img=$img",
            college = college,
            course = course,
            year = year,
            verification = VerificationState(true, verified, false),
            rating = 4.2 + (img % 6) * 0.1,
            listingsCount = 5 + img,
            soldCount = img
        )

    val neha = student("u1", "Neha Singh", 5, "ABC Engineering College", "Electronics", "3rd Year")
    val rohit = student("u2", "Rohit Kumar", 8, "ABC Engineering College", "Mechanical", "1st Year")
    val rahul = student("u3", "Rahul Verma", 15, "City Polytechnic", "CSE", "2nd Year")
    val aditya = student("u4", "Aditya Singh", 22, "City Polytechnic", "ECE", "3rd Year")
    val priya = student("u5", "Priya Nair", 29, "ABC Engineering College", "CSE", "1st Year")
    val pgOwner = student("u6", "PG Owner", 33, "N/A", "N/A", "N/A", verified = false)

    /** All known mock students, keyed by id — used to resolve a Student from just an id. */
    val allStudents: List<Student> = listOf(currentUser, neha, rohit, rahul, aditya, priya, pgOwner)

    val listings: List<Listing> = listOf(
        Listing(
            id = "l1", title = "Wooden Study Table", price = 1500,
            category = ListingCategory.FURNITURE, subCategory = "Tables",
            condition = ListingCondition.GOOD, distanceKm = 0.8, imageRes = 0,
            description = "Sturdy wooden study table with drawer. 8 months old. Good condition. Perfect for students.",
            postedDaysAgo = 2, seller = currentUser
        ),
        Listing(
            id = "l2", title = "Ergonomic Chair", price = 2200,
            category = ListingCategory.FURNITURE, subCategory = "Chairs",
            condition = ListingCondition.LIKE_NEW, distanceKm = 0.9, imageRes = 1,
            description = "Mesh-back ergonomic chair, adjustable height, barely used this semester.",
            postedDaysAgo = 1, seller = neha
        ),
        Listing(
            id = "l3", title = "Bookshelf", price = 1000,
            category = ListingCategory.FURNITURE, subCategory = "Storage",
            condition = ListingCondition.GOOD, distanceKm = 1.1, imageRes = 2,
            description = "5-tier wooden bookshelf, fits comfortably in a hostel room corner.",
            postedDaysAgo = 4, seller = rohit
        ),
        Listing(
            id = "l4", title = "Single Bed", price = 2500,
            category = ListingCategory.FURNITURE, subCategory = "Beds",
            condition = ListingCondition.GOOD, distanceKm = 1.3, imageRes = 3,
            description = "Single bed frame with slats, no mattress. Easy to disassemble for moving.",
            postedDaysAgo = 6, seller = rahul
        ),
        Listing(
            id = "l5", title = "Washing Machine", price = 3000,
            category = ListingCategory.APPLIANCES, subCategory = "Appliances",
            condition = ListingCondition.USED, distanceKm = 1.4, imageRes = 4,
            description = "Semi-automatic, 6kg. Works perfectly, selling because I'm moving into a PG with laundry.",
            postedDaysAgo = 3, seller = aditya
        ),
        Listing(
            id = "l6", title = "Study Lamp", price = 350,
            category = ListingCategory.ELECTRONICS, subCategory = "Lighting",
            condition = ListingCondition.LIKE_NEW, distanceKm = 0.5, imageRes = 5,
            description = "LED desk lamp, 3 brightness modes, USB powered.",
            postedDaysAgo = 1, seller = priya
        ),
        Listing(
            id = "l7", title = "Data Structures Textbook", price = 400,
            category = ListingCategory.BOOKS, subCategory = "CSE",
            condition = ListingCondition.GOOD, distanceKm = 0.6, imageRes = 6,
            description = "Standard DSA reference, some highlighting in chapters 1-4.",
            postedDaysAgo = 5, seller = rahul
        ),
        Listing(
            id = "l8", title = "Mini Refrigerator", price = 4500,
            category = ListingCategory.APPLIANCES, subCategory = "Appliances",
            condition = ListingCondition.GOOD, distanceKm = 2.0, imageRes = 7,
            description = "45L mini fridge, perfect for a hostel room. Cools well, minor dent on the side.",
            postedDaysAgo = 7, seller = neha
        ),
        Listing(
            id = "l9", title = "Office Chair (Black)", price = 1800,
            category = ListingCategory.FURNITURE, subCategory = "Chairs",
            condition = ListingCondition.USED, distanceKm = 1.7, imageRes = 8,
            description = "Sturdy office chair, wheels work fine, fabric slightly worn.",
            postedDaysAgo = 8, seller = rohit
        ),
        Listing(
            id = "l10", title = "Bluetooth Speaker", price = 900,
            category = ListingCategory.ELECTRONICS, subCategory = "Audio",
            condition = ListingCondition.LIKE_NEW, distanceKm = 0.4, imageRes = 9,
            description = "JBL clone, great bass, includes charging cable.",
            postedDaysAgo = 2, seller = priya
        ),
        Listing(
            id = "l11", title = "Steel Almirah", price = 3200,
            category = ListingCategory.FURNITURE, subCategory = "Storage",
            condition = ListingCondition.GOOD, distanceKm = 1.9, imageRes = 10,
            description = "2-door steel almirah with lock, minor surface rust on the base.",
            postedDaysAgo = 9, seller = aditya
        )
    )

    val roomListings: List<RoomListing> = listOf(
        RoomListing(
            id = "r1", title = "Single Room in 2BHK", rentPerMonth = 6000,
            type = RoomType.SINGLE, furnishing = FurnishState.FURNISHED,
            distanceKm = 1.2, imageRes = 20, imageUrl = null,
            description = "Private room in a shared 2BHK flat, 5 min walk from campus gate.",
            postedDaysAgo = 3, owner = neha
        ),
        RoomListing(
            id = "r2", title = "PG for Boys", rentPerMonth = 5500,
            type = RoomType.PG, furnishing = FurnishState.FURNISHED, foodIncluded = true,
            distanceKm = 0.9, imageRes = 21, imageUrl = null,
            description = "Boys PG, 3 meals included, WiFi, laundry service twice a week.",
            postedDaysAgo = 1, owner = pgOwner
        ),
        RoomListing(
            id = "r3", title = "2BHK Flat", rentPerMonth = 12000,
            type = RoomType.FLAT, furnishing = FurnishState.SEMI_FURNISHED,
            distanceKm = 1.5, imageRes = 22, imageUrl = null,
            description = "Full 2BHK flat, ideal for 2-3 students sharing, semi-furnished with wardrobes.",
            postedDaysAgo = 5, owner = rahul
        ),
        RoomListing(
            id = "r4", title = "1RK Near Campus", rentPerMonth = 4000,
            type = RoomType.SINGLE, furnishing = FurnishState.UNFURNISHED,
            distanceKm = 0.8, imageRes = 23, imageUrl = null,
            description = "Compact 1RK, unfurnished, water and electricity included in rent.",
            postedDaysAgo = 6, owner = aditya
        ),
        RoomListing(
            id = "r5", title = "Shared Room in PG", rentPerMonth = 3500,
            type = RoomType.SHARED, furnishing = FurnishState.FURNISHED, foodIncluded = true,
            distanceKm = 1.1, imageRes = 24, imageUrl = null,
            description = "Twin sharing room, food included, common study area on the same floor.",
            postedDaysAgo = 4, owner = pgOwner
        ),
        RoomListing(
            id = "r6", title = "Girls PG Near Metro", rentPerMonth = 6500,
            type = RoomType.PG, furnishing = FurnishState.FURNISHED, foodIncluded = true,
            distanceKm = 1.4, imageRes = 25, imageUrl = null,
            description = "Girls-only PG, 2 min from metro station, CCTV and warden on premises.",
            postedDaysAgo = 2, owner = priya
        ),
        RoomListing(
            id = "r7", title = "Studio Flat", rentPerMonth = 9000,
            type = RoomType.FLAT, furnishing = FurnishState.FURNISHED,
            distanceKm = 2.1, imageRes = 26, imageUrl = null,
            description = "Independent studio flat, fully furnished, ideal for a single student.",
            postedDaysAgo = 8, owner = rohit
        ),
        RoomListing(
            id = "r8", title = "Single Room + Balcony", rentPerMonth = 7000,
            type = RoomType.SINGLE, furnishing = FurnishState.SEMI_FURNISHED,
            distanceKm = 1.6, imageRes = 27, imageUrl = null,
            description = "Bright single room with a small balcony, quiet residential lane.",
            postedDaysAgo = 3, owner = neha
        )
    )

    val myPreferences = RoommatePreferences(
        budgetMin = 5000, budgetMax = 7000,
        sleepSchedule = SleepSchedule.LATE,
        smoking = YesNo.NO,
        food = FoodPref.VEG,
        cleanliness = CleanlinessLevel.HIGH,
        pets = YesNo.NO,
        studyEnvironment = StudyEnvironment.QUIET,
        maxDistanceKm = 3.0
    )

    val roommateCandidates: List<RoommateCandidate> = listOf(
        RoommateCandidate(
            student = rahul,
            preferences = RoommatePreferences(5000, 6500, SleepSchedule.LATE, YesNo.NO, FoodPref.VEG, CleanlinessLevel.HIGH, YesNo.NO, StudyEnvironment.QUIET),
            distanceKm = 1.2
        ),
        RoommateCandidate(
            student = aditya,
            preferences = RoommatePreferences(5500, 7000, SleepSchedule.LATE, YesNo.NO, FoodPref.EITHER, CleanlinessLevel.MEDIUM, YesNo.NO, StudyEnvironment.MODERATE),
            distanceKm = 1.5
        ),
        RoommateCandidate(
            student = rohit,
            preferences = RoommatePreferences(4000, 5500, SleepSchedule.EARLY_BIRD, YesNo.YES, FoodPref.NON_VEG, CleanlinessLevel.LOW, YesNo.YES, StudyEnvironment.SOCIAL),
            distanceKm = 2.4
        ),
        RoommateCandidate(
            student = priya,
            preferences = RoommatePreferences(5500, 7500, SleepSchedule.FLEXIBLE, YesNo.NO, FoodPref.VEG, CleanlinessLevel.HIGH, YesNo.NO, StudyEnvironment.QUIET),
            distanceKm = 0.9
        )
    )

    val conversations: List<Conversation> = listOf(
        Conversation(
            id = "c1", participant = rahul, lastMessage = "Hey, is the room still available?",
            time = "10:30 AM", unreadCount = 2, context = ChatFilter.ROOMS,
            messages = listOf(
                ChatMessage("m1", rahul.id, "Hi! Saw your room listing.", "10:20 AM", isMine = false),
                ChatMessage("m2", rahul.id, "Hey, is the room still available?", "10:30 AM", isMine = false, isRead = false)
            )
        ),
        Conversation(
            id = "c2", participant = currentUser, lastMessage = "Yes, you can check it tomorrow.",
            time = "9:15 AM", unreadCount = 0, context = ChatFilter.ROOMS,
            messages = listOf(
                ChatMessage("m3", currentUser.id, "Yes, you can check it tomorrow.", "9:15 AM", isMine = true)
            )
        ),
        Conversation(
            id = "c3", participant = pgOwner, lastMessage = "Thanks for your interest.",
            time = "Yesterday", unreadCount = 0, context = ChatFilter.ROOMS,
            messages = listOf(
                ChatMessage("m4", pgOwner.id, "Thanks for your interest.", "Yesterday", isMine = false)
            )
        ),
        Conversation(
            id = "c4", participant = neha, lastMessage = "Is the table available?",
            time = "Yesterday", unreadCount = 1, context = ChatFilter.MARKETPLACE,
            messages = listOf(
                ChatMessage("m5", neha.id, "Is the table available?", "Yesterday", isMine = false, isRead = false)
            )
        ),
        Conversation(
            id = "c5", participant = rohit, lastMessage = "Okay, will ping you.",
            time = "2 days ago", unreadCount = 0, context = ChatFilter.MARKETPLACE,
            messages = listOf(
                ChatMessage("m6", rohit.id, "Okay, will ping you.", "2 days ago", isMine = false)
            )
        )
    )

    val notifications: List<AppNotification> = listOf(
        AppNotification("n1", "Rahul Verma", rahul.avatarUrl, "sent you a message", "10:30 AM", NotificationType.MESSAGE, actorId = rahul.id),
        AppNotification("n2", "Your listing \"Study Table\"", "", "got 5 new views", "9:45 AM", NotificationType.VIEW),
        AppNotification("n3", "Neha Singh", neha.avatarUrl, "liked your listing \"Wooden Chair\"", "Yesterday", NotificationType.LIKE, actorId = neha.id),
        AppNotification("n4", "Your listing", "", "has a new offer", "Yesterday", NotificationType.OFFER),
        AppNotification("n5", "PG Owner", pgOwner.avatarUrl, "replied to your message", "2 days ago", NotificationType.REPLY, isUnread = false, actorId = pgOwner.id),
        AppNotification("n6", "Aditya Singh", aditya.avatarUrl, "sent you a message", "3 days ago", NotificationType.MESSAGE, isUnread = false, actorId = aditya.id),
        AppNotification("n7", "Your listing \"Bookshelf\"", "", "got 3 new views", "4 days ago", NotificationType.VIEW, isUnread = false),
        AppNotification("n8", "Priya Nair", priya.avatarUrl, "liked your listing \"Study Lamp\"", "5 days ago", NotificationType.LIKE, isUnread = false, actorId = priya.id)
    )
}