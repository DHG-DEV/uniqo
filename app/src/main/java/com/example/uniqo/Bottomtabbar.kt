package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

// 5 destinations per spec: Home, Marketplace, center Post, Chat, Profile — no Rooms.
val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Filled.Home),
    BottomTab(Routes.MARKET, "Market", Icons.Filled.Storefront),
    BottomTab(Routes.CHAT_LIST, "Chat", Icons.Filled.Chat),
    BottomTab(Routes.PROFILE, "Profile", Icons.Filled.Person),
)

@Composable
fun UniqoBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    hasUnreadChat: Boolean = false,
    profileAvatarUrl: String? = null
) {
    // SIZE ADJUSTMENTS (was 88.dp / 72.dp / 56.dp / 54.dp) — shrunk the whole bar.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // Soft purple glow behind the center button (halo/notch effect).
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(PurplePrimary.copy(alpha = 0.16f))
        )

        // Floating pill-shaped bar — uses the dedicated bottom-nav background
        // color per spec (#F0EEFF), distinct from CardWhite used elsewhere.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(22.dp), clip = false)
                .clip(RoundedCornerShape(22.dp))
                .background(BottomNavBackground)
                .height(40.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.take(2).forEach { tab ->
                BottomBarTabItem(tab, currentRoute, onTabSelected, profileAvatarUrl)
            }
            Spacer(Modifier.width(38.dp))
            bottomTabs.drop(2).forEach { tab ->
                BottomBarTabItem(tab, currentRoute, onTabSelected, profileAvatarUrl)
            }
        }

        // Floating center Post button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(40.dp)
                .shadow(elevation = 8.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(PurplePrimary)
                .clickable { onTabSelected(Routes.POST_LISTING) },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "Post",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BottomBarTabItem(
    tab: BottomTab,
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    profileAvatarUrl: String?
) {
    val selected = currentRoute == tab.route
    val isProfileTab = tab.route == Routes.PROFILE
    // ADD-ON: muted gray-purple for unselected tabs per spec, instead of plain TextSecondary.
    val unselectedTint = TabUnselected

    Column(
        modifier = Modifier
            .clickable { onTabSelected(tab.route) }
            .padding(horizontal = 3.dp, vertical = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isProfileTab && !profileAvatarUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(if (selected) 16.dp else 13.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) Modifier.border(1.dp, PurplePrimary, CircleShape) else Modifier
                    )
                    .padding(if (selected) 1.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = profileAvatarUrl,
                    contentDescription = tab.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        } else {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = if (selected) PurplePrimary else unselectedTint,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            tab.label,
            fontSize = 9.sp,
            color = if (selected) PurplePrimary else unselectedTint
        )
    }
}