package com.example.uniqo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaticContentScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            content = content
        )
    }
}

@Composable
private fun ContentCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardWhite)
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun ContentSection(heading: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(heading, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun BulletRow(icon: ImageVector, text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    StaticContentScaffold("Privacy Policy", onBack) {
        ContentCard {
            ContentSection("Information We Collect", "We collect information you provide directly, such as your name, college, contact details, listings, and messages, along with usage data needed to operate Uniqo.")
            ContentSection("How We Use Your Information", "Your information is used to connect you with roommates and buyers/sellers on campus, keep the marketplace safe, and improve the app.")
            ContentSection("Sharing of Information", "We do not sell your personal data. Limited information (like your name and profile photo) is shared with other users as part of normal app functionality.")
            ContentSection("Data Retention", "We retain your data while your account is active. You can request deletion of your account and data at any time from Settings.")
            ContentSection("Your Choices", "You control what appears on your profile and can update or delete your information at any time.")
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun TermsConditionsScreen(onBack: () -> Unit) {
    StaticContentScaffold("Terms & Conditions", onBack) {
        ContentCard {
            ContentSection("Acceptance of Terms", "By using Uniqo, you agree to these terms. If you do not agree, please do not use the app.")
            ContentSection("Eligibility", "Uniqo is intended for verified students. You are responsible for the accuracy of information provided during verification.")
            ContentSection("User Conduct", "You agree not to post fraudulent listings, harass other users, or misuse the platform in any way that violates our Community Guidelines.")
            ContentSection("Transactions", "Uniqo facilitates connections between users but is not a party to any transaction, rental agreement, or roommate arrangement made through the app.")
            ContentSection("Termination", "We may suspend or terminate accounts that violate these terms or our Community Guidelines.")
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun CommunityGuidelinesScreen(onBack: () -> Unit) {
    StaticContentScaffold("Community Guidelines", onBack) {
        ContentCard {
            BulletRow(Icons.Default.Block, "No harassment, hate speech, or abusive content toward any user.")
            BulletRow(Icons.Default.ReportProblem, "No spam, fake listings, or misleading information.")
            BulletRow(Icons.Default.Warning, "No fraud, scams, or requests for advance payment outside safe channels.")
            BulletRow(Icons.Default.PersonOff, "No impersonation of other users, staff, or institutions.")
            BulletRow(Icons.Default.Chat, "Communicate respectfully in chat with other students.")
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun SafetyCenterScreen(onBack: () -> Unit) {
    StaticContentScaffold("Safety Center", onBack) {
        ContentCard {
            BulletRow(Icons.Default.Groups, "Meet people in safe, public places, especially the first time.")
            BulletRow(Icons.Default.Lock, "Avoid sharing sensitive personal information like ID numbers or bank details in chat.")
            BulletRow(Icons.Default.VerifiedUser, "Verify a user's profile before making important arrangements.")
            BulletRow(Icons.Default.Payments, "Be cautious with advance payments — never pay before verifying a listing or person.")
            BulletRow(Icons.Default.Flag, "Report suspicious users or listings so we can take action.")
            BulletRow(Icons.Default.Block, "Block anyone who makes you uncomfortable — you're always in control.")
        }
        Spacer(Modifier.height(20.dp))
    }
}

private data class FaqItem(val question: String, val answer: String)

private val faqItems = listOf(
    FaqItem("How do I create a listing?", "Tap the + button on Home or Market, fill in the details, and post."),
    FaqItem("How do roommate matches work?", "Set your Roommate Preferences and Uniqo suggests compatible students based on lifestyle, budget, and location."),
    FaqItem("How do I message someone?", "Open a listing, room, or profile and tap Message or Chat to start a conversation."),
    FaqItem("How do I report a listing?", "Open the listing and use the report option, or contact support with the listing details."),
    FaqItem("How do I delete my account?", "Go to Settings → Delete Account and follow the confirmation steps."),
    FaqItem("How does verification work?", "We verify your college email, phone number, and student ID to confirm you're a real student.")
)

@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    StaticContentScaffold("Help & Support", onBack) {
        Text("Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(10.dp))

        ContentCard {
            faqItems.forEachIndexed { index, item ->
                FaqRow(item)
                if (index != faqItems.lastIndex) HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Contact Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(10.dp))

        ContentCard {
            Text("Need more help? Reach out and we'll get back to you as soon as possible.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("support@uniqo.app", color = PurplePrimary, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun FaqRow(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(item.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
        }
        if (expanded) {
            Spacer(Modifier.height(6.dp))
            Text(item.answer, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}