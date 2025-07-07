package org.ethereumphone.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.neonOpacity
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.components.DgenLoadingMatrix
import org.ethereumphone.dgenlibrary.components.dgenButton
import org.ethereumphone.dgenlibrary.components.dgenTextButton
import org.ethereumphone.dgenlibrary.theme.DgenTheme
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.os.Build.VERSION.SDK_INT
import org.ethereumphone.dgenlibrary.screens.InformationScreen

@Composable
fun OnboardingRoute(
    onFinishOnboarding: () -> Unit,
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {

    val syncState by onboardingViewModel.syncState.collectAsState()
    val isOnline by onboardingViewModel.isOnline.collectAsState(initial = true)



    OnboardingScreen(
        syncState = syncState,
        isOnline = isOnline,
        onStartXmtp = onboardingViewModel::generateXMTP,
        onStartSync = onboardingViewModel::startFirstSync,
        onFinishOnboarding = {
            onboardingViewModel.hideOnboarding(it)
            onFinishOnboarding()
        }

    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    syncState: SyncState,
    isOnline: Boolean,
    onStartXmtp: () -> Unit,
    onStartSync: () -> Unit,
    onFinishOnboarding: (useXmtp: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pageContent = OnboardingPageContent.entries

    val pagerState = rememberPagerState(
        pageCount = { OnboardingPageContent.entries.size},
    )


    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    // Build a Coil image loader that supports GIFs, required for InformationScreen animations
    val gifEnabledLoader = remember(context) {
        ImageLoader.Builder(context).components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()
    }

    // Show the information screen whenever the device is offline, regardless of current sync state
    val isOffline = !isOnline

    AnimatedContent(
        targetState = isOffline,
        transitionSpec = {
            fadeIn(animationSpec = tween(300, 300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        modifier = Modifier.fillMaxHeight().background(dgenBlack)
    ) { offline ->
        if (offline) {
            // Show an informational screen prompting the user to connect to the internet.
            InformationScreen(
                gifEnabledLoader = gifEnabledLoader,
                primaryColor = primaryColor,
                text = "Connect to the internet to proceed"
            )
        } else {
            when(syncState) {
                is SyncState.Success -> {
                    LaunchedEffect(Unit) {
                        val job = coroutineScope.async { onStartSync() }
                        job.await()
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
                is SyncState.Error -> {
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            pagerState.scrollToPage(pageContent.size - 1) // error page
                        }
                    }
                }
                else -> {}
            }



            HorizontalPager(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(dgenBlack),
                state = pagerState,
                userScrollEnabled = false
            ) {
                PagerContent(pageContent[pagerState.currentPage], primaryColor
                ) {
                    when(pagerState.currentPage) {
                        0 -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                dgenButton(
                                    backgroundColor = primaryColor,
                                    fontColor = secondaryColor,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            onStartXmtp()
                                            delay(500)
                                        }
                                    },
                                    text = "Enable XMTP"
                                )

                                dgenTextButton(
                                    fontColor = primaryColor,
                                    onClick = { onFinishOnboarding(false) },
                                    text = "Skip",
                                    primaryColor = primaryColor
                                )
                            }



                        }
                        1 -> DgenLoadingMatrix(
                            unactiveLEDColor = secondaryColor,
                            activeLEDColor = primaryColor
                        )

                        2,3 -> {
                            dgenButton(
                                backgroundColor = primaryColor,
                                fontColor = secondaryColor,
                                onClick = {
                                    if (syncState is SyncState.Success) {
                                        onFinishOnboarding(true)
                                    } else {
                                        onFinishOnboarding(false)
                                    }
                                },
                                text = "Finish Setup"
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun PagerContent(
    pagerContent: OnboardingPageContent,
    primaryColor: Color,
    extraContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text(
            text = pagerContent.title,
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontFamily = PitagonsSans,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 40.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            ),
            modifier = Modifier

        )

        Text(
            text = pagerContent.description,
//            style = TextStyle(
//                fontFamily = PitagonsSans,
//                color = dgenTurqoise,
//                fontWeight = FontWeight.Normal,
//                fontSize = 18.sp,
//                lineHeight = 18.sp,
//                letterSpacing = 0.sp,
//                textDecoration = TextDecoration.None,
//                textAlign = TextAlign.Center
//            ),
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = primaryColor.copy(neonOpacity),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None,
                textAlign = TextAlign.Center
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(350.dp).padding(top = 16.dp, bottom = 48.dp)
        )
        Spacer(Modifier.height(10.dp))

        extraContent()
    }
}


enum class OnboardingPageContent(
    val title: String,
    val description: String
) {
    WELCOME(
        "Chat with XMTP",
        "XMTP enables secure, wallet-to-wallet messaging using Ethereum addresses."
    ),
    SYNC("Settings things up", "Please, sign the next two prompts"),
    FINISH("All set up :)", "You can change XMTP behaviours via the messenger's options"),
    ERROR("Something went wrong","You can retry via the option menu")
}


@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice",uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun previewOnboarding() {
    Column(Modifier.background(Color.Black)) {
        OnboardingScreen(SyncState.Loading, true, {}, {}, {})
    }
}
