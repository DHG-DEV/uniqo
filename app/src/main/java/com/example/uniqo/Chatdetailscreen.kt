package com.example.uniqo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    repository: UniqoRepository,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val conversations by repository.conversations.collectAsState()
    val conversation = conversations.firstOrNull { it.id == conversationId } ?: return

    var input by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun handlePickedUri(uri: Uri) {
        scope.launch {
            uploading = true
            try {
                val resolver = context.contentResolver
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }

                if (bytes == null || bytes.isEmpty()) {
                    Toast.makeText(context, "Couldn't read that file.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val mimeType = resolver.getType(uri) ?: "application/octet-stream"
                val fileName = getFileName(context, uri)

                repository.sendAttachmentMessage(
                    conversationId = conversationId,
                    bytes = bytes,
                    fileName = fileName,
                    mimeType = mimeType
                )
            } catch (e: Exception) {
                android.util.Log.e("UNIQO_CHAT", "Attachment failed", e)
                Toast.makeText(
                    context,
                    "Couldn't send that file.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                uploading = false
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handlePickedUri(it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { handlePickedUri(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { handlePickedUri(it) }
        }
        pendingCameraUri = null
    }

    fun launchCamera() {
        try {
            val uri = createChatCameraImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            android.util.Log.e("UNIQO_CHAT", "Camera launch failed", e)
            Toast.makeText(
                context,
                "Couldn't open camera.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(
                context,
                "Camera permission is needed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showAttachmentSheet) {
        AlertDialog(
            onDismissRequest = { showAttachmentSheet = false },
            title = { Text("Share") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showAttachmentSheet = false
                            openCamera()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📷  Camera")
                    }

                    TextButton(
                        onClick = {
                            showAttachmentSheet = false
                            try {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    )
                                )
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Couldn't open gallery.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🖼  Photos & Videos")
                    }

                    TextButton(
                        onClick = {
                            showAttachmentSheet = false
                            try {
                                filePicker.launch("*/*")
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Couldn't open file picker.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📎  Files")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showAttachmentSheet = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(conversation.messages.size) {
        if (conversation.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Row(
                        modifier = Modifier.clickable {
                            onOpenProfile(conversation.participant.id)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            url = conversation.participant.avatarUrl,
                            size = 34
                        )

                        Spacer(Modifier.width(10.dp))

                        Column {
                            Text(
                                text = conversation.participant.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            PulsePresence(
                                lastActiveAt = conversation.participant.lastActiveAt
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = {
                                showMenu = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Block User") },
                                onClick = {
                                    showMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Report User") },
                                onClick = {
                                    showMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardWhite
                )
            )
        },
        bottomBar = {
            Column {
                if (uploading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWhite)
                        .padding(
                            horizontal = 12.dp,
                            vertical = 10.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = !uploading,
                        onClick = {
                            showAttachmentSheet = true
                        }
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            tint = TextSecondary
                        )
                    }

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = {
                            Text("Message...")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !uploading,
                        singleLine = true
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        enabled = input.isNotBlank() && !uploading,
                        onClick = {
                            val text = input.trim()

                            if (text.isNotEmpty()) {
                                repository.sendMessage(
                                    conversationId,
                                    text
                                )

                                input = ""

                                scope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = PurplePrimary
                        )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = conversation.messages.reversed(),
                key = { it.id }
            ) { message ->
                MessageBubble(
                    message = message,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isMine = message.isMine
    val bubbleColor = if (isMine) PurplePrimary else CardWhite
    val contentColor = if (isMine) Color.White else TextPrimary
    val alignment = if (isMine) Alignment.End else Alignment.Start

    val shape = if (isMine) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 2.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 2.dp,
            bottomEnd = 16.dp
        )
    }

    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (
            message.messageType == "image" &&
            !message.fileUrl.isNullOrBlank()
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 220.dp)
                    .clip(shape)
                    .background(PastelBlue)
                    .clickable {
                        openAttachment(
                            context,
                            message.fileUrl,
                            message.fileMimeType
                        )
                    }
            ) {
                AsyncImage(
                    model = message.fileUrl,
                    contentDescription = message.fileName ?: "Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = 140.dp,
                            max = 260.dp
                        )
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(shape)
                    .background(bubbleColor)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
            ) {
                when (message.messageType) {
                    "video" -> AttachmentCard(
                        icon = "▶",
                        title = message.fileName ?: "Video",
                        subtitle = "Video",
                        contentColor = contentColor
                    ) {
                        openAttachment(
                            context,
                            message.fileUrl,
                            message.fileMimeType
                        )
                    }

                    "pdf" -> AttachmentCard(
                        icon = "📄",
                        title = message.fileName ?: "PDF document",
                        subtitle = "PDF",
                        contentColor = contentColor
                    ) {
                        openAttachment(
                            context,
                            message.fileUrl,
                            message.fileMimeType
                        )
                    }

                    "file" -> AttachmentCard(
                        icon = "📎",
                        title = message.fileName ?: "File",
                        subtitle = message.fileMimeType ?: "File",
                        contentColor = contentColor
                    ) {
                        openAttachment(
                            context,
                            message.fileUrl,
                            message.fileMimeType
                        )
                    }

                    else -> {
                        Text(
                            text = message.text,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = message.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun AttachmentCard(
    icon: String,
    title: String,
    subtitle: String,
    contentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = contentColor.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private fun getFileName(
    context: Context,
    uri: Uri
): String {
    var name: String? = null

    context.contentResolver.query(
        uri,
        arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(
                android.provider.OpenableColumns.DISPLAY_NAME
            )

            if (index >= 0) {
                name = cursor.getString(index)
            }
        }
    }

    return name
        ?: uri.lastPathSegment
        ?: "attachment"
}

private fun createChatCameraImageUri(
    context: Context
): Uri {
    val imagesDir = File(
        context.cacheDir,
        "images"
    ).apply {
        mkdirs()
    }

    val imageFile = File.createTempFile(
        "chat_${System.currentTimeMillis()}_",
        ".jpg",
        imagesDir
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

private fun openAttachment(
    context: Context,
    url: String?,
    mimeType: String?
) {
    if (url.isNullOrBlank()) return

    try {
        val uri = Uri.parse(url)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            if (!mimeType.isNullOrBlank()) {
                setDataAndType(uri, mimeType)
            } else {
                data = uri
            }

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e(
            "UNIQO_CHAT",
            "Could not open attachment",
            e
        )

        Toast.makeText(
            context,
            "No app can open this file.",
            Toast.LENGTH_SHORT
        ).show()
    }
}