package org.ethereumphone.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.LineHeightStyle.Trim
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun OnboardingRoute() {

    OnboardingScreen()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen() {

    val coroutineScope = rememberCoroutineScope()
    val pageContent = OnboardingPageContent.entries

    val pagerState = rememberPagerState(
        pageCount = { OnboardingPageContent.entries.size},
    )
    HorizontalPager(
        modifier = Modifier.fillMaxHeight(),
        state = pagerState,
        userScrollEnabled = false
    ) {
        PagerContent(
            pageContent[pagerState.currentPage]
        ) {
            when(pagerState.currentPage) {
                0 -> {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } }
                    ) {
                        Text("Enable XMTP")
                    }

                    Text(
                        text = "Skip",
                        color = Color.White,
                        modifier = Modifier.clickable {

                        }
                    )
                }
            }
        }
    }

}

@Composable
private fun PagerContent(
    pagerContent: OnboardingPageContent,
    extraContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text(
            text = pagerContent.title,
            style = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
            ),
            modifier = Modifier.padding(vertical = 5.dp)

        )

        Text(
            text = pagerContent.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
                textAlign = TextAlign.Center
            )
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
        "XMTP is a secure messaging protocol that allows you to communicate via eth addresses"
    ),
    SYNC("Settings things up", "Please, sign the next two prompts"),
    FINISH("All set up", "")
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun previewOnboarding() {
    Column(Modifier.background(Color.Black)) {
        OnboardingScreen()

    }
}
