package org.ethereumhpone.chat.util

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.datetime.Instant
import org.ethereumphone.model.Contact
import kotlin.time.Duration.Companion.seconds
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient


import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import org.ethereumphone.model.Attachment
import org.ethereumphone.model.Reaction
import org.ethosmobile.components.library.models.TransferItem
import kotlin.random.Random

@Composable
fun generateRandomTransfers(): List<TransferItem> {
    val random = Random(System.currentTimeMillis())

    // Beispielhafte Listen für zufällige Werte
    val possibleChainIds = listOf(1, 56, 137, 42)  // z.B. Ethereum, BSC, Polygon, Kovan
    val possibleAssets = listOf("DAI","ETH")

    // Beispiel-Adressen (typisch 0x + 40 Hex-Stellen, hier verkürzt oder zufällig generiert)
    val sampleAddresses = listOf(
        "emunsi.eth",
        "0x4e83362442B8d1beC281594cEa3050c8EB01311C",
        "0xC0fFee0000000000000000000000000000000000",
        "0x7Bb4fC5D2f9afE98Ed7be9cEB49F2C4dA333b0B3",
        "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB",
        "0x1111111254EEB25477B68fb85Ed929f73A960582"
    )

    // Beispiel-Zeitstempel im Format YYYY-MM-dd HH:mm:ss, hier stark vereinfacht
    val sampleTimeStamps = listOf(
        "2023-01-10 14:25:13",
        "2023-02-11 09:41:22",
        "2023-03-15 22:07:59",
        "2023-03-16 01:33:45",
        "2023-03-17 16:00:00"
    )

    // Beispiel-Hashes (typisch 0x + 64 Hex-Stellen)
    val sampleTxHashes = listOf(
        "0xaaaabbbbccccddddeeeeffff1111222233334444555566667777888899990000",
        "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
        "0xbad00bad00bad00bad00bad00bad00bad00bad00bad00bad00bad00bad00bad0",
        "0x7777777777777777777777777777777777777777777777777777777777777777",
        "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
    )

    // Erzeuge mindestens 10 Einträge
    return List(10) {
        TransferItem(
            chainId = possibleChainIds.random(random),
            from = sampleAddresses.random(random),
            to = sampleAddresses.random(random),
            asset = possibleAssets.random(random),
            value = (random.nextInt(1, 1000) + random.nextDouble()).toString(),
            timeStamp = sampleTimeStamps.random(random),
            userSent = random.nextBoolean(),
            txHash = sampleTxHashes.random(random),
        )
    }
}
fun generateTestMessages(): List<Message> {
    val recipientMe = Recipient(
        id = "userA",
        address = "0xAbC123CryptoBroWallet",
        ens = "bro.eth",
        contact = null
    )

    val recipientOther = Recipient(
        id = "userB",
        address = "0xDeF456HodlGuyWallet",
        ens = "hodl.eth",
        contact = Contact("lk2", "Bob", null, "0x456")
    )

    val recipientOther2 = Recipient(
        id = "userC",
        address = "0xDeF456JoeGuy",
        ens = "Joe.eth",
        contact = Contact("lk2", "Joe", null, "0x474")
    )

    val now = Instant.parse("2025-04-17T12:23:05Z")

    return listOf(
        Message("1", "thread123", recipientOther, now - (60 * 60).seconds, now - (60 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Yo bro, did you see what \$SOL did today??"),
        Message("2", "thread123", recipientMe, now - (59 * 60).seconds, now - (59 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "🚀🚀 I told you, man. It's alt season again!"),
        Message("3", "thread123", recipientOther, now - (58 * 60).seconds, now - (58 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Bro I was THIS close to aping in yesterday and chickened out 😭"),
        Message("4", "thread123", recipientMe, now - (57 * 60).seconds, now - (57 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Classic paper hands move 😂"),
        Message("5", "thread123", recipientOther, now - (56 * 60).seconds, now - (56 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Don’t remind me. Even my grandma made a 2x on PEPE."),
        Message("6", "thread123", recipientMe, now - (55 * 60).seconds, now - (55 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Wait what? Granny got in before you? 😂😂"),
        Message("7", "thread123", recipientOther, now - (54 * 60).seconds, now - (54 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "I'm honestly thinking of letting her manage my whole portfolio 😂"),
        Message("7a", "thread123", recipientOther, now - (53 * 60).seconds, now - (53 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "I'm honestly thinking of letting her manage my whole portfolio 😂"),
        Message("8", "thread123", recipientMe, now - (52 * 60).seconds, now - (52 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Lmao, vibes-based investing always wins in this market."),
        Message("9", "thread123", recipientOther, now - (50 * 60).seconds, now - (50 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "I'm done trying to TA this madness. Just gonna vibe like granny."),
        Message("10", "thread123", recipientMe, now - (48 * 60).seconds, now - (48 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Speaking of vibes… did you mint that L2 NFT drop this morning?"),
        Message("11", "thread123", recipientOther, now - (47 * 60).seconds, now - (47 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Nah bro gas was wild. \$70 just to mint? Miss me with that. 😒"),
        Message("11a", "thread123", recipientOther, now - (46 * 60).seconds, now - (46 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Like… that’s a whole meal and a coffee in fiat bro 😂"),
        Message("12", "thread123", recipientMe, now - (45 * 60).seconds, now - (45 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Fair. I queued at 3am, got in, and flipped one already tho 👀"),
        Message("13", "thread123", recipientOther, now - (44 * 60).seconds, now - (44 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Bro you’re on another level 😂 teach me your ways"),
        Message("14", "thread123", recipientMe, now - (42 * 60).seconds, now - (42 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Step 1: never sleep 😎"),
        Message("15", "thread123", recipientOther, now - (41 * 60).seconds, now - (41 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Step 2?"),
        Message("15a", "thread123", recipientOther, now - (40 * 60).seconds, now - (40 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Or is it just hope and cope from here on? 😂"),
        Message("16", "thread123", recipientMe, now - (39 * 60).seconds, now - (39 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Step 2: rotate into memecoins and pray to the green candle gods."),
        Message("17", "thread123", recipientOther, now - (38 * 60).seconds, now - (38 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "🙏 May the pumps be ever in your favor."),
        Message("18", "thread123", recipientMe, now - (36 * 60).seconds, now - (36 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Amen 🙏 btw we still on for that ETH meetup Friday?"),
        Message("19", "thread123", recipientOther, now - (35 * 60).seconds, now - (35 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Of course. Already polishing my Ledger and MetaMask hoodie 😎"),
        Message("20", "thread123", recipientMe, now - (34 * 60).seconds, now - (34 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "LFG 🔥")
    )
}

fun generateTestGroupMessages(): List<Message> {
    val recipientMe = Recipient(
        id = "userA",
        address = "0xAbC123CryptoBroWallet",
        ens = "bro.eth",
        contact = Contact("lk1", "Timothy", null, "0x423")
    )

    val recipientOther2 = Recipient(
        id = "userB",
        address = "0xDeF456HodlGuyWallet",
        ens = "hodl.eth",
        contact = Contact("lk2", "Bob", null, "0x456")
    )

    val recipientOther = Recipient(
        id = "userC",
        address = "0xDeF456JoeGuy",
        ens = "Joe.eth",
        contact = Contact("lk3", "Joe", null, "0x474")
    )

    val past = Instant.parse("2025-04-15T12:00:00Z")
    val now  = Instant.parse("2025-04-17T12:00:00Z")

    return listOf(
            Message("1","thread3x3",recipientOther,past-(60*60).seconds,past-(60*60).seconds,true,DeliveryStatus.PUBLISHED,null,false,emptyList(),emptyList(),"Hey Bob, are we all set for the Freedom Factory 3x3 tournament tomorrow? Our first game is at 9 AM on Court 5."),
            Message("2","thread3x3",recipientMe,past-(59*60).seconds,past-(59*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"Yeah, I’m in! Where should we meet for the team warm‑up?"),
            Message("3","thread3x3",recipientOther2,past-(58*60).seconds,past-(58*60).seconds,true,DeliveryStatus.PUBLISHED,null,false,emptyList(),emptyList(),"Joe, you good for tomorrow’s 3x3 games? Freedom Factory crew is doing a quick intro shoot at 8:30."),
            Message("4","thread3x3",recipientMe,past-(57*60).seconds,past-(57*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"All good here. I’ll be there by 8 AM, got the new sponsor shoes ready 🏀"),
            Message("5","thread3x3",recipientOther,past-(56*60).seconds,past-(56*60).seconds,true,DeliveryStatus.PUBLISHED,null,false,emptyList(),emptyList(),"Great. Let’s meet at the east entrance of the sports hall at 8. Team name on the board will say ‘Freedom Dunkers’."),
            Message("6","thread3x3",recipientMe,past-(55*60).seconds,past-(55*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"Perfect. Do you think we should change our jerseys or stick with the old ones?"),
            Message("7","thread3x3",recipientOther,past-(54*60).seconds,past-(54*60).seconds,true,DeliveryStatus.PUBLISHED,null,false,emptyList(),emptyList(),"What jersey color do you think the Freedom Factory logo pops on most? White with blue trim, or blue with white?"),
            Message("7a","thread3x3",recipientOther,past-(53*60).seconds,past-(53*60).seconds,true,DeliveryStatus.PUBLISHED,null,false,emptyList(),emptyList(),"Also, let’s register our team name as ‘Freedom Dunkers’ on the app by tonight."),
            Message("8","thread3x3",recipientMe,past-(52*60).seconds,past-(52*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"Blue with white trim sounds sharp. Just tagged it in the registration."),
            Message("9","thread3x3",recipientMe,past-(51*60).seconds,past-(51*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"I’ll bring extra water and snacks for the team. Freedom Factory said they’d supply some energy bars too."),
            Message("10","thread3x3",recipientOther,now-(60*60).seconds,now-(60*60).seconds,true,DeliveryStatus.PUBLISHED,null,false,emptyList(),emptyList(),"Day 1 recap: our defense was tight, but offense lagged. Thoughts?"),
            Message("11","thread3x3",recipientMe,now-(59*60).seconds,now-(59*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"Agreed! Our pick‑and‑roll was on fire once we found our rhythm."),
            Message("11a","thread3x3",recipientMe,now-(58*60).seconds,now-(58*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"Plus, the Freedom Factory merch stand was a hit with those hoodies."),
            Message("12","thread3x3",recipientOther2,now-(57*60).seconds,now-(57*60).seconds,true,DeliveryStatus.PUBLISHED,null,false,emptyList(),emptyList(),"Joe, how did those new kicks feel? Did they give you that extra hop for rebounds?"),
            Message("13","thread3x3",recipientMe,now-(56*60).seconds,now-(56*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"Absolutely, felt light. Also got some cool Freedom Factory headbands from the booth."),
            Message("14","thread3x3",recipientOther,now-(55*60).seconds,now-(55*60).seconds,true,DeliveryStatus.PUBLISHED,null,false,emptyList(),emptyList(),"Next game is at 4 PM on Court 2. You free to run drills at lunch?"),
            Message("15","thread3x3",recipientMe,now-(54*60).seconds,now-(54*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"Yeah, let’s do it. I’m thinking shooting drills then a quick scrimmage."),
            Message("15a","thread3x3",recipientMe,now-(53*60).seconds,now-(53*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"Should we meet at the free‑throw line spot around 12:30?"),
            Message("16","thread3x3",recipientOther2,now-(52*60).seconds,now-(52*60).seconds,true,DeliveryStatus.PUBLISHED,null,false,emptyList(),emptyList(),"Sounds good. I’ll be there with the ball pump and extra towels."),
            Message("17","thread3x3",recipientMe,now-(51*60).seconds,now-(51*60).seconds,true,DeliveryStatus.PUBLISHED,null,true,emptyList(),emptyList(),"Got it! And I’ll post some stories tagging Freedom Factory later.")
    )
}

val testMessage = Message(
    id = "msg_123",
    threadId = "thread_456",
    recipient = Recipient(
        id = "user_789",
        address = "rrehstgrq",
        ens = "bro.eth",
        contact = Contact("lk3", "Timothy", null, "0x474")
    ),
    date = Instant.parse("2025-04-23T12:00:00Z"),
    dateSent = Instant.parse("2025-04-23T11:59:00Z"),
    seen = false,
    deliveryStatus = DeliveryStatus.PUBLISHED,
    replyReference = null,
    isMe = true,
    attachments = listOf(
       /*
        Attachment(
         id = "att_1",
            type = "image/png",
            url = "https://example.com/image.png"
        )
        */

    ),
    reactions = emptyList(),
    body = "Hallo, das hier ist eine Testnachricht!"
)







/*
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(preloaded: List<String> = emptyList()) {
    // 1) Create ChatMessage objects for your history…
    val initialMsgs = remember { preloaded.map { ChatMessage(text = it) } }
    // 2) Backing list, seeded with history:
    val messages = remember { mutableStateListOf<ChatMessage>().apply { addAll(initialMsgs) } }
    // 3) Visibility map: mark history as already visible = true
    val visibleMap = remember {
        mutableStateMapOf<Long, MutableState<Boolean>>().apply {
            initialMsgs.forEach { put(it.id, mutableStateOf(true)) }
        }
    }
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.weight(1f)
        ) {
            items(items = messages, key = { it.id }) { msg ->


                    // each msg gets its own MutableState<Boolean>
                    val isVisible = visibleMap.getOrPut(msg.id) { mutableStateOf(false) }

                    AnimatedVisibility(
                        visible = isVisible.value,
                        enter = scaleIn(
                            initialScale = 0f,
                            transformOrigin = TransformOrigin(0f, 1f),
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(tween(300)),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier.animateItemPlacement(animationSpec = tween(durationMillis = 300))

                    ) {
                        MessageBubble(msg.text)
                    }



                // only flip to true if it wasn’t already (history stays true)
                LaunchedEffect(msg.id) {
                    if (!isVisible.value) {
                        isVisible.value = true
                    }
                }
            }
        }

        Row(Modifier.padding(8.dp)) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (input.isNotBlank()) {
                    val newMsg = ChatMessage(text = input)
                    // start invisible so it will animate in
                    visibleMap[newMsg.id] = mutableStateOf(false)
                    messages.add(0, newMsg)
                    input = ""

                }
            }) {
                Text("Send")
            }
            LaunchedEffect(messages.size) {
                listState.animateScrollToItem(0)
            }
        }
    }
}

// same as before
data class ChatMessage(val id: Long = Random.nextLong(), val text: String)

@Composable
fun MessageBubble(text: String) {
    Box(
        Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFDCF8C6))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen(listOf("Bruh", "Hallow"))
    }
}
 */


