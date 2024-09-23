package org.ethereumphone.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@Composable
fun OnboardingRoute() {

    OnboardingScreen()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen() {


    val pageContent = OnboardingPageContent.entries

    val pagerState = rememberPagerState(
        pageCount = { OnboardingPageContent.entries.size},
    )



    Column {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false
        ) {

        }

        val coroutineScope = rememberCoroutineScope()

        Row {
            if (pagerState.currentPage > 0) {
                Text(
                    text = "Back",
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                )
            }


            if (pagerState.currentPage != pagerState.pageCount -1) {
                Text(
                    text = "Next",
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }

                )
            }

        }
    }
}

private fun OnboardingPage(

) {

}

enum class OnboardingPageContent(
    title: String,
    description: String
) {
    WELCOME("Welcome to ethOS Messenger", ""),
    XMTP("Enable XMTP", ""),
    FINISH("All set up", "")
}


@Preview
@Composable
fun previewOnboarding() {
    OnboardingScreen()
}
