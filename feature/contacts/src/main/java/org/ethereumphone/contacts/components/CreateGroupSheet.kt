package org.ethereumphone.contacts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.DgenLoadingMatrix
import com.example.dgenlibrary.SimpleDgenTextfield
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.backgrounds.FadeDirection
import com.example.dgenlibrary.ui.backgrounds.FadeEdge
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body2_fontSize
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.label_fontSize
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import com.messenger.terminalsdk.TerminalSDK
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumhpone.database.model.ContactEntity

@Composable
fun CreateGroupSheet(
    members: List<ContactEntity>,
    onBackClick: () -> Unit,
    onCreateGroup: (List<ContactEntity>, String) -> Unit,
    isCreationFailed: Boolean = false,
    primaryColor: Color = dgenTurqoise,
    secondaryColor: Color = dgenOcean
) {
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var isCreating by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val view = LocalView.current

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val terminalSDK = remember {
        try { TerminalSDK(context) } catch (_: Exception) { null }
    }

    LaunchedEffect(isCreationFailed) {
        if (isCreationFailed && isCreating) {
            isCreating = false
            delay(100)
            try {
                if (terminalSDK?.isAvailable() == true) {
                    terminalSDK.removeConfirm()
                }
            } catch (_: Exception) { }
        }
    }

    val hasGroupName = textState.text.isNotBlank()
    DisposableEffect(hasGroupName, isCreating, isCreationFailed) {
        if (hasGroupName && !isCreating && !isCreationFailed) {
            coroutineScope.launch {
                try {
                    if (terminalSDK?.isAvailable() == true) {
                        terminalSDK.displayConfirm {
                            if (textState.text.isNotBlank() && !isCreating) {
                                isCreating = true
                                onCreateGroup(members, textState.text.trim())
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            coroutineScope.launch {
                try {
                    if (terminalSDK?.isAvailable() == true) {
                        terminalSDK.removeConfirm()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        onDispose {
            coroutineScope.launch {
                try {
                    if (terminalSDK?.isAvailable() == true) {
                        terminalSDK.removeConfirm()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        var isFirstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isFirstResume) {
                    isFirstResume = false
                    return@LifecycleEventObserver
                }
                if (textState.text.isNotBlank() && !isCreating) {
                    coroutineScope.launch {
                        delay(500)
                        try {
                            if (terminalSDK?.isAvailable() == true) {
                                terminalSDK.displayConfirm {
                                    if (textState.text.isNotBlank() && !isCreating) {
                                        isCreating = true
                                        onCreateGroup(members, textState.text.trim())
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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

                        FadeEdge(FadeDirection.Top, Modifier.align(Alignment.TopCenter), size = 8.dp)
                        FadeEdge(FadeDirection.Bottom, Modifier.align(Alignment.BottomCenter), size = 8.dp)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isCreating,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dgenBlack)
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    DgenLoadingMatrix(
                        unactiveLEDColor = secondaryColor,
                        activeLEDColor = primaryColor
                    )
                    Text(
                        text = "CREATING GROUP...",
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = label_fontSize
                        )
                    )
                }
            }
        }
    }
}