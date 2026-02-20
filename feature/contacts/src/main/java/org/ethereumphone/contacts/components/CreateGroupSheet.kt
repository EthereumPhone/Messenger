package org.ethereumphone.contacts.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.SimpleDgenTextfield
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body2_fontSize
import com.example.dgenlibrary.ui.theme.label_fontSize
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumhpone.database.model.ContactEntity

@Composable
fun CreateGroupSheet(
    members: List<ContactEntity>,
    onBackClick: () -> Unit,
    onCreateGroup: (List<ContactEntity>, String) -> Unit,
    primaryColor: Color = dgenTurqoise,
    secondaryColor: Color = dgenOcean
) {
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var isCreating by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val view = LocalView.current

    val buttonAlpha by animateFloatAsState(
        if (textState.text.isEmpty() || isCreating) 0.35f else 1f,
        tween(300)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        DgenHeaderBackground(
            title = "CREATE GROUP",
            primaryColor = primaryColor,
            focusManager = focusManager,
            onBackClick = { if (!isCreating) onBackClick() }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, bottom = 24.dp)
            ) {
                SimpleDgenTextfield(
                    value = textState,
                    onValueChange = { if (!isCreating) textState = it },
                    keyboardtype = KeyboardType.Text,
                    onEditDone = { },
                    enabled = !isCreating,
                    singleLine = true,
                    cursorColor = primaryColor,
                    activeColor = primaryColor,
                    view = view,
                    textfieldFocusManager = focusManager,
                    placeholder = {
                        Text(
                            text = "Type group name",
                            style = TextStyle(
                                fontFamily = PitagonsSans,
                                color = primaryColor.copy(alpha = 0.45f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = body2_fontSize,
                            )
                        )
                    },
                    labelContent = {
                        Text(
                            text = "GROUP NAME",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = label_fontSize
                            )
                        )
                    }
                )

                Surface(
                    color = primaryColor,
                    shape = CircleShape,
                    modifier = Modifier
                        .height(25.dp)
                        .alpha(buttonAlpha)
                        .clickable(enabled = textState.text.isNotBlank() && !isCreating) {
                            if (textState.text.isNotBlank() && !isCreating) {
                                isCreating = true
                                onCreateGroup(members, textState.text.trim())
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = secondaryColor,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "CREATING...",
                                color = secondaryColor,
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    lineHeight = 16.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        } else {
                            Text(
                                text = "CREATE",
                                color = secondaryColor,
                                style = TextStyle(
                                    fontFamily = SpaceMono,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    lineHeight = 16.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MEMBERS ${members.size}",
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    )
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(members) { contact ->
                                MemberItem(
                                    header = contact.name.ifBlank {
                                        contact.ethAddress?.let { addr ->
                                            when {
                                                addr.endsWith(".eth") -> addr
                                                addr.length > 16 -> addr.take(8) + "..." + addr.takeLast(6)
                                                else -> addr
                                            }
                                        } ?: contact.lookupKey
                                    },
                                    subheader = if (contact.name.isNotBlank()) {
                                        contact.ethAddress?.let { addr ->
                                            when {
                                                addr.endsWith(".eth") -> addr
                                                addr.length > 10 -> addr.take(6) + "..." + addr.takeLast(6)
                                                else -> addr
                                            }
                                        } ?: ""
                                    } else "",
                                    onDelete = {},
                                    primaryColor = primaryColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .align(Alignment.TopCenter)
                            .background(Brush.verticalGradient(listOf(dgenBlack, Color.Transparent))))
                        Spacer(modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, dgenBlack))))
                    }
                }
            }
        }

        if (isCreating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dgenBlack.copy(alpha = 0.7f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = primaryColor,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "Creating group...",
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    )
                }
            }
        }
    }
}