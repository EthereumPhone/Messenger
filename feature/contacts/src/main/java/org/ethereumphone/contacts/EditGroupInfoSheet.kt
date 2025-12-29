package org.ethereumphone.contacts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.ghostOpacity
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.neonOpacity
import kotlinx.coroutines.delay
import org.ethereumphone.dgenlibrary.components.SecondaryScreenHeader
import org.ethereumphone.dgenlibrary.components.SimpleDgenTextfield
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumhpone.database.model.ContactEntity

@Composable
fun EditGroupInfoSheet(
    onDismiss: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    groupName: String,
    groupImageUri: Uri?,
    members: List<ContactEntity>,
    onGroupNameChanged: (String) -> Unit,
    onGroupImageChanged: (Uri?) -> Unit,
    onMemberRemoved: (String) -> Unit, // ethAddress
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current

    // Group name state
    var groupNameState by remember { mutableStateOf(TextFieldValue(groupName)) }
    LaunchedEffect(groupName) {
        if (groupNameState.text != groupName) {
            groupNameState = TextFieldValue(groupName)
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onGroupImageChanged(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            SecondaryScreenHeader(
                title = "EDIT GROUP".uppercase(),
                primaryColor = primaryColor,
                onDismiss = onDismiss,
            )

            Spacer(Modifier.fillMaxWidth().height(16.dp))

            // Group Image Field
            GroupImageField(
                imageUri = groupImageUri,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                onPickImage = {
                    focusManager.clearFocus()
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                // Optional: Add haptic feedback or other action
                            }
                        )
                    }
            )

            Spacer(Modifier.fillMaxWidth().height(16.dp))

            // Group Name Field
            SimpleDgenTextfield(
                value = groupNameState,
                onValueChange = { newValue ->
                    groupNameState = newValue
                    onGroupNameChanged(newValue.text)
                },
                keyboardtype = KeyboardType.Text,
                textfieldFocusManager = focusManager,
                onEditDone = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                view = view,
                placeholder = {
                    Text(
                        text = "GROUP NAME".uppercase(),
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor.copy(0.45f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = label_fontSize
                        )
                    )
                },
                labelContent = {
                    Text(
                        text = "GROUP NAME".uppercase(),
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = label_fontSize
                        )
                    )
                }
            )

            Spacer(Modifier.fillMaxWidth().height(16.dp))

            // Members Count
            Text(
                text = buildAnnotatedString {
                    append("MEMBERS ")
                    withStyle(
                        style = SpanStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        append("${members.size}")
                    }
                },
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            )

            // Members List
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(members) { contact ->
                        val ethAddr = contact.ethAddress?.trim().orEmpty()
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = PitagonsSans,
                                        color = primaryColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 22.sp,
                                        lineHeight = 22.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    )
                                )

                                if (ethAddr.isNotBlank()) {
                                    Text(
                                        text = when {
                                            ethAddr.endsWith(".eth") -> ethAddr
                                            ethAddr.length > 10 -> ethAddr.take(6) + "..." + ethAddr.takeLast(6)
                                            else -> ethAddr
                                        },
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            fontFamily = PitagonsSans,
                                            color = dgenWhite,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            lineHeight = 16.sp,
                                            letterSpacing = 0.sp,
                                            textDecoration = TextDecoration.None
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Remove",
                                tint = primaryColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        if (ethAddr.isNotBlank()) {
                                            onMemberRemoved(ethAddr)
                                        }
                                    }
                            )
                        }
                    }

                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(dgenBlack, Color.Transparent)
                            )
                        )
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, dgenBlack)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun GroupImageField(
    imageUri: Uri?,
    primaryColor: Color,
    secondaryColor: Color,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Track pressed state to animate background
    var isPressed by remember { mutableStateOf(false) }

    // Automatically reset pressed state after short delay
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(300) // matches the tween duration below
            isPressed = false
        }
    }

    val animatedBgAlpha by animateFloatAsState(
        targetValue = if (isPressed) ghostOpacity else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "bgAlpha"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(0.dp))
            .drawBehind {
                // Draw animated background the size of the composable
                drawRect(
                    color = dgenOcean,
                    size = size,
                    topLeft = Offset.Zero,
                    alpha = animatedBgAlpha
                )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Label
        Text(
            text = "GROUP IMAGE".uppercase(),
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = label_fontSize
            )
        )

        // Image area – 112dp square box, left-aligned
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(secondaryColor, RoundedCornerShape(8.dp))
                .clickable {
                    isPressed = true
                    onPickImage()
                },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Group Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Select Image",
                        modifier = Modifier.size(28.dp),
                        tint = primaryColor.copy(neonOpacity)
                    )
                    Text(
                        text = "UPLOAD",
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor.copy(neonOpacity),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}
