package com.example.uniqo

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.launch

@Composable
fun UniqoNavGraph() {
    val navController = rememberNavController()
    val repository = RepositoryProvider.repository
    val scope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Log.d("STARTUP", "navgraph effect START ${System.currentTimeMillis()}")
        val userId = AuthManager.awaitInitialUserId()
        Log.d("STARTUP", "got userId=$userId ${System.currentTimeMillis()}")
        if (userId != null) {
            (repository as? SupabaseRepository)?.onAuthenticated()
            Log.d("STARTUP", "onAuthenticated done ${System.currentTimeMillis()}")
            startDestination = Routes.HOME
        } else {
            startDestination = Routes.WELCOME
        }
        Log.d("STARTUP", "startDestination set ${System.currentTimeMillis()}")
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    if (startDestination == null) return

    val bottomRoutes = setOf(
        Routes.HOME,
        Routes.MARKET,
        Routes.ROOMS,
        Routes.CHAT_LIST,
        Routes.PROFILE
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                val conversations by repository.conversations.collectAsState()
                val user by repository.currentUserProfile.collectAsState()

                UniqoBottomBar(
                    currentRoute = currentRoute,
                    hasUnreadChat = conversations.any { it.unreadCount > 0 },
                    profileAvatarUrl = user.avatarUrl,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onGetStarted = { navController.navigate(Routes.SIGNUP) },
                    onLogin = { navController.navigate(Routes.LOGIN) }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onBack = { navController.popBackStack() },
                    onLoggedIn = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    },
                    onGoToSignup = { navController.navigate(Routes.SIGNUP) }
                )
            }

            composable(Routes.SIGNUP) {
                VerifySerialDobScreen(
                    onBack = { navController.popBackStack() },
                    onVerified = { serial, dob ->
                        navController.navigate("signup_create/$serial/$dob")
                    }
                )
            }

            composable(
                "signup_create/{serial}/{dob}",
                arguments = listOf(
                    navArgument("serial") { type = NavType.StringType },
                    navArgument("dob") { type = NavType.StringType }
                )
            ) { entry ->
                CreateAccountScreen(
                    serialNumber = entry.arguments?.getString("serial") ?: "",
                    dobDdMmYyyy = entry.arguments?.getString("dob") ?: "",
                    onBack = { navController.popBackStack() },
                    onOtpSent = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    repository = repository,
                    onSearchClick = { navController.navigate(Routes.MARKET) },
                    onCategoryClick = {
                        repository.setCategoryFilter(it)
                        navController.navigate(Routes.MARKET)
                    },
                    onSeeAllCategories = { navController.navigate(Routes.MARKET) },
                    onListingClick = { navController.navigate(Routes.listingDetail(it)) },
                    onRoomClick = { navController.navigate(Routes.roomDetail(it)) },
                    onSeeAllRooms = { navController.navigate(Routes.ROOMS) },
                    onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                    onPostClick = { navController.navigate(Routes.POST_LISTING) },
                    onProfileClick = { navController.navigate(Routes.PROFILE) },
                    onLookingForClick = {
                        navController.navigate(Routes.LOOKING_FOR_CATEGORY_SELECT)
                    }
                )
            }

            composable(Routes.MARKET) {
                MarketplaceScreen(
                    repository = repository,
                    onListingClick = {
                        navController.navigate(Routes.listingDetail(it))
                    },
                    onFilterClick = {
                        navController.navigate(Routes.FILTERS)
                    }
                )
            }

            composable(Routes.ROOMS) {
                RoomsScreen(
                    repository = repository,
                    onRoomClick = {
                        navController.navigate(Routes.roomDetail(it))
                    }
                )
            }

            composable(Routes.CHAT_LIST) {
                ChatListScreen(
                    repository = repository,
                    onConversationClick = {
                        navController.navigate(Routes.chatDetail(it))
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onEditProfile = {
                        navController.navigate(Routes.EDIT_PROFILE)
                    },
                    onMenuClick = { action ->
                        when (action) {
                            "logout" -> {
                                // Keep your existing logout implementation here.
                                // Navigation is intentionally not changed.
                            }

                            "roommate_matches",
                            "roommate_preferences" ->
                                navController.navigate(Routes.ROOMMATE_MATCH)

                            Routes.CHAT_LIST ->
                                navController.navigate(Routes.CHAT_LIST)

                            "saved_roommate_profiles",
                            "roommate_requests",
                            "roommate_match_history" ->
                                navController.navigate(Routes.ROOMMATE_MATCH)

                            "my_offers",
                            "purchase_history",
                            "selling_history",
                            "recently_viewed",
                            "reviews_received",
                            "blocked_users",
                            "invite_friends",
                            "privacy_safety",
                            "help",
                            "about_uniqo" ->
                                navController.navigate(action)

                            else ->
                                navController.navigate(action)
                        }
                    }
                )
            }

            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(Routes.MY_LISTINGS) {
                MyListingsScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onListingClick = {
                        navController.navigate(Routes.listingDetail(it))
                    }
                )
            }

            composable(Routes.BOOKMARKS) {
                BookmarksScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onListingClick = {
                        navController.navigate(Routes.listingDetail(it))
                    }
                )
            }

            // ---------------------------------------------------------
            // LOOKING FOR
            // ---------------------------------------------------------

            composable(Routes.LOOKING_FOR_CATEGORY_SELECT) {
                LookingForCategoryScreen(
                    onBack = { navController.popBackStack() },
                    onCategorySelected = { category ->
                        navController.navigate(Routes.lookingForForm(category))
                    }
                )
            }

            composable(
                Routes.LOOKING_FOR_FORM,
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType },
                    navArgument("prefId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entry ->
                val categoryName =
                    entry.arguments?.getString("category") ?: ""

                val prefId =
                    entry.arguments?.getString("prefId")
                        ?.takeIf { it.isNotBlank() }

                val category =
                    LookingForCategory.entries.find {
                        it.name == categoryName
                    } ?: LookingForCategory.OTHER

                val preferences by repository.lookingForPreferences.collectAsState()

                val existing =
                    prefId?.let { id ->
                        preferences.firstOrNull { it.id == id }
                    }

                LookingForFormScreen(
                    category = category,
                    existing = existing,
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(Routes.ROOMMATE_MATCH) {
                RoommateMatchScreen(
                    repository = repository,
                    onViewProfile = {
                        navController.navigate(Routes.roommateProfile(it))
                    },
                    onChat = { candidateId ->
                        val candidate =
                            repository.roommateCandidates()
                                .firstOrNull {
                                    it.student.id == candidateId
                                }

                        val conversationId =
                            repository.findOrCreateConversation(
                                userId = candidateId,
                                name = candidate?.student?.name ?: "User",
                                avatarUrl = candidate?.student?.avatarUrl ?: ""
                            )

                        navController.navigate(
                            Routes.chatDetail(conversationId)
                        )
                    }
                )
            }

            composable(
                Routes.ROOMMATE_PROFILE,
                arguments = listOf(
                    navArgument("candidateId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val id =
                    entry.arguments?.getString("candidateId") ?: ""

                RoommateProfileScreen(
                    candidateId = id,
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onChat = { candidateId ->
                        val candidate =
                            repository.roommateCandidates()
                                .firstOrNull {
                                    it.student.id == candidateId
                                }

                        val conversationId =
                            repository.findOrCreateConversation(
                                userId = candidateId,
                                name = candidate?.student?.name ?: "User",
                                avatarUrl = candidate?.student?.avatarUrl ?: ""
                            )

                        navController.navigate(
                            Routes.chatDetail(conversationId)
                        )
                    }
                )
            }

            composable(
                Routes.LISTING_DETAIL,
                arguments = listOf(
                    navArgument("listingId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val id =
                    entry.arguments?.getString("listingId") ?: ""

                ListingDetailScreen(
                    listingId = id,
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onChatSeller = { sellerId ->
                        val listing =
                            repository.listings.value.firstOrNull {
                                it.id == id
                            }

                        val conversationId =
                            repository.findOrCreateConversation(
                                userId = sellerId,
                                name = listing?.seller?.name ?: "User",
                                avatarUrl = listing?.seller?.avatarUrl ?: ""
                            )

                        navController.navigate(
                            Routes.chatDetail(conversationId)
                        )
                    }
                )
            }

            composable(
                Routes.ROOM_DETAIL,
                arguments = listOf(
                    navArgument("roomId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val id =
                    entry.arguments?.getString("roomId") ?: ""

                RoomDetailScreen(
                    roomId = id,
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onContactOwner = { ownerId ->
                        val room =
                            repository.roomListings.value.firstOrNull {
                                it.id == id
                            }

                        val conversationId =
                            repository.findOrCreateConversation(
                                userId = ownerId,
                                name = room?.owner?.name ?: "User",
                                avatarUrl = room?.owner?.avatarUrl ?: ""
                            )

                        navController.navigate(
                            Routes.chatDetail(conversationId)
                        )
                    }
                )
            }

            composable(
                Routes.CHAT_DETAIL,
                arguments = listOf(
                    navArgument("conversationId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val id =
                    entry.arguments?.getString("conversationId") ?: ""

                ChatDetailScreen(
                    conversationId = id,
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onOpenProfile = {
                        navController.navigate(
                            Routes.userProfile(it)
                        )
                    }
                )
            }

            composable(
                Routes.USER_PROFILE,
                arguments = listOf(
                    navArgument("userId") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val userId =
                    entry.arguments?.getString("userId") ?: ""

                UserProfileScreen(
                    userId = userId,
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onChat = { studentId ->
                        val student =
                            repository.listings.value.firstOrNull {
                                it.seller.id == studentId
                            }?.seller
                                ?: repository.roomListings.value.firstOrNull {
                                    it.owner.id == studentId
                                }?.owner
                                ?: repository.roommateCandidates()
                                    .firstOrNull {
                                        it.student.id == studentId
                                    }?.student
                                ?: repository.conversations.value.firstOrNull {
                                    it.participant.id == studentId
                                }?.participant

                        val conversationId =
                            repository.findOrCreateConversation(
                                userId = studentId,
                                name = student?.name ?: "User",
                                avatarUrl = student?.avatarUrl ?: ""
                            )

                        navController.navigate(
                            Routes.chatDetail(conversationId)
                        )
                    },
                    onOpenListing = {
                        navController.navigate(
                            Routes.listingDetail(it)
                        )
                    },
                    onOpenRoom = {
                        navController.navigate(
                            Routes.roomDetail(it)
                        )
                    }
                )
            }

            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onOpenChats = {
                        navController.navigate(Routes.CHAT_LIST)
                    },
                    onOpenMyListings = {
                        navController.navigate(Routes.MY_LISTINGS)
                    },
                    onOpenListing = {
                        navController.navigate(Routes.listingDetail(it))
                    },
                    onChatSeller = { userId, name, avatarUrl ->
                        val conversationId =
                            repository.findOrCreateConversation(
                                userId = userId,
                                name = name,
                                avatarUrl = avatarUrl
                            )

                        navController.navigate(
                            Routes.chatDetail(conversationId)
                        )
                    },
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS)
                    }
                )
            }

            composable(Routes.POST_LISTING) {
                PostListingScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onPosted = { navController.popBackStack() }
                )
            }

            composable(Routes.FILTERS) {
                val filters by repository.activeFilters.collectAsState()

                FiltersScreen(
                    initial = filters,
                    onBack = { navController.popBackStack() },
                    onApply = {
                        repository.updateFilters(it)
                        navController.popBackStack()
                    }
                )
            }

            // ---------------------------------------------------------
            // SETTINGS
            // ---------------------------------------------------------

            // ---------------------------------------------------------
            // LOCATION PICKER
            // ---------------------------------------------------------

            composable(Routes.MAP) {
                ProfileLocationPickerScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigate = { route ->
                        navController.navigate(route)
                    },
                    onLogout = {
                        scope.launch {
                            AuthManager.signOut()
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.ACCOUNT_INFO) {
                AccountInformationScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onEditProfile = {
                        navController.navigate(Routes.EDIT_PROFILE)
                    }
                )
            }

            composable(Routes.EMAIL_PHONE) {
                EmailPhoneScreen(
                    repository = repository,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.CHANGE_PASSWORD) {
                ChangePasswordScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.DELETE_ACCOUNT) {
                DeleteAccountScreen(
                    onBack = { navController.popBackStack() },
                    onAccountDeleted = {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.SAFETY_CENTER) {
                SafetyCenterScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.COMMUNITY_GUIDELINES) {
                CommunityGuidelinesScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PRIVACY_POLICY) {
                PrivacyPolicyScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.TERMS_CONDITIONS) {
                TermsConditionsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HELP_SUPPORT) {
                HelpSupportScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ---------------------------------------------------------
            // PHASE 3 — NEW ROUTES (Notifications / Appearance / Language / Location)
            // ---------------------------------------------------------

            composable(Routes.NOTIFICATION_SETTINGS) {
                NotificationSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.APPEARANCE) {
                AppearanceScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.LANGUAGE) {
                LanguageScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.LOCATION_PREFERENCES) {
                LocationPreferencesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
