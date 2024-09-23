package org.ethereumphone.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun OnboardingRoute() {

    OnboardingScreen()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen() {


    val pagerState = rememberPagerState(
        pageCount = { OnboardingPageContent.entries.size},
    )



    Column() {
        HorizontalPager(pagerState) { }




        Row {
            if (pagerState.currentPage > 0) {
                Text("Back")
            }


            if (pagerState.currentPage != pagerState.pageCount -1) {
                Text("Next")
            }

        }
    }


}





enum class OnboardingPageContent(
    title: String,
    description: String
) {
    WELCOME("", ""),
    XMTP("", ""),
}