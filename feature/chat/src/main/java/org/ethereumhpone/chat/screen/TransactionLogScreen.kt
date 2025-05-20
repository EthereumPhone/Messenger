package org.ethereumhpone.chat.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ethereumhpone.chat.components.TransationLog
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethosmobile.components.library.models.TransferItem

@Composable
fun TransactionLogScreen(
    scrollState: LazyListState,
    transactions: List<TransferItem> = emptyList()
){
    LazyColumn(
        state= scrollState,
        modifier = Modifier
            // Apply the scrollbar first
            .fillMaxSize()
            .padding(start = 32.dp, end = 0.dp, top = 0.dp)
            .verticalLazyListScrollbar(scrollState,fixed= true),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
        }

        items(transactions) { transfer ->
            //TODO: Add real tx's
            TransationLog(logEntry = transfer)
        }


        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}