package org.ethereumhpone.chat.util

import kotlinx.datetime.Instant
import org.ethereumphone.model.Contact
import kotlin.time.Duration.Companion.seconds
import org.ethereumphone.model.DeliveryStatus
import org.ethereumphone.model.Message
import org.ethereumphone.model.Recipient

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

    val past = Instant.parse("2025-04-12T12:23:05Z")
    val now = Instant.parse("2025-04-17T12:23:05Z")

    return listOf(
        Message("1", "thread123", recipientOther, past - (60 * 60).seconds, past - (60 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Yo bro, did you see what \$SOL did today??"),
        Message("2", "thread123", recipientMe, past - (59 * 60).seconds, past - (59 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "🚀🚀 I told you, man. It's alt season again!"),
        Message("3", "thread123", recipientOther, past - (58 * 60).seconds, past - (58 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Bro I was THIS close to aping in yesterday and chickened out 😭"),
        Message("4", "thread123", recipientMe, past - (57 * 60).seconds, past - (57 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Classic paper hands move 😂"),
        Message("5", "thread123", recipientOther, past - (56 * 60).seconds, past - (56 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Don’t remind me. Even my grandma made a 2x on PEPE."),
        Message("6", "thread123", recipientMe, past - (55 * 60).seconds, past - (55 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Wait what? Granny got in before you? 😂😂"),
        Message("7", "thread123", recipientOther, past - (54 * 60).seconds, past - (54 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "I'm honestly thinking of letting her manage my whole portfolio 😂"),
        Message("7a", "thread123", recipientOther, past - (53 * 60).seconds, past - (53 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "I'm honestly thinking of letting her manage my whole portfolio 😂"),
        Message("8", "thread123", recipientMe, past - (52 * 60).seconds, past - (52 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Lmao, vibes-based investing always wins in this market."),
        Message("9", "thread123", recipientOther, past - (50 * 60).seconds, past - (50 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "I'm done trying to TA this madness. Just gonna vibe like granny."),
        Message("10", "thread123", recipientMe, past - (48 * 60).seconds, past - (48 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Speaking of vibes… did you mint that L2 NFT drop this morning?"),
        Message("11", "thread123", recipientOther, past - (47 * 60).seconds, past - (47 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Nah bro gas was wild. \$70 just to mint? Miss me with that. 😒"),
        Message("11a", "thread123", recipientOther, past - (46 * 60).seconds, past - (46 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Like… that’s a whole meal and a coffee in fiat bro 😂"),
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

    val past = Instant.parse("2025-04-12T12:23:05Z")
    val now = Instant.parse("2025-04-17T12:23:05Z")

    return listOf(
        Message("1", "thread123", recipientOther2, past - (60 * 60).seconds, past - (60 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Yo bro, did you see what \$SOL did today??"),
        Message("2", "thread123", recipientMe, past - (59 * 60).seconds, past - (59 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "🚀🚀 I told you, man. It's alt season again!"),
        Message("3", "thread123", recipientOther, past - (58 * 60).seconds, past - (58 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Bro I was THIS close to aping in yesterday and chickened out 😭"),
        Message("4", "thread123", recipientMe, past - (57 * 60).seconds, past - (57 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Classic paper hands move 😂"),
        Message("5", "thread123", recipientOther2, past - (56 * 60).seconds, past - (56 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Don’t remind me. Even my grandma made a 2x on PEPE."),
        Message("6", "thread123", recipientMe, past - (55 * 60).seconds, past - (55 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Wait what? Granny got in before you? 😂😂"),
        Message("7", "thread123", recipientOther, past - (54 * 60).seconds, past - (54 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "I'm honestly thinking of letting her manage my whole portfolio 😂"),
        Message("7a", "thread123", recipientOther, past - (53 * 60).seconds, past - (53 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "I'm honestly thinking of letting her manage my whole portfolio 😂"),
        Message("8", "thread123", recipientMe, past - (52 * 60).seconds, past - (52 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Lmao, vibes-based investing always wins in this market."),
        Message("9", "thread123", recipientOther2, past - (50 * 60).seconds, past - (50 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "I'm done trying to TA this madness. Just gonna vibe like granny."),
        Message("10", "thread123", recipientMe, past - (48 * 60).seconds, past - (48 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Speaking of vibes… did you mint that L2 NFT drop this morning?"),
        Message("11", "thread123", recipientOther2, past - (47 * 60).seconds, past - (47 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Nah bro gas was wild. \$70 just to mint? Miss me with that. 😒"),
        Message("11a", "thread123", recipientOther2, past - (46 * 60).seconds, past - (46 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Like… that’s a whole meal and a coffee in fiat bro 😂"),
        Message("12", "thread123", recipientMe, now - (45 * 60).seconds, now - (45 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Fair. I queued at 3am, got in, and flipped one already tho 👀"),
        Message("13", "thread123", recipientOther, now - (44 * 60).seconds, now - (44 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Bro you’re on another level 😂 teach me your ways"),
        Message("14", "thread123", recipientMe, now - (42 * 60).seconds, now - (42 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Step 1: never sleep 😎"),
        Message("15", "thread123", recipientOther, now - (41 * 60).seconds, now - (41 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Step 2?"),
        Message("15a", "thread123", recipientOther, now - (40 * 60).seconds, now - (40 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Or is it just hope and cope from here on? 😂"),
        Message("16", "thread123", recipientMe, now - (39 * 60).seconds, now - (39 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Step 2: rotate into memecoins and pray to the green candle gods."),
        Message("17", "thread123", recipientOther2, now - (38 * 60).seconds, now - (38 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "🙏 May the pumps be ever in your favor."),
        Message("18", "thread123", recipientMe, now - (36 * 60).seconds, now - (36 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "Amen 🙏 btw we still on for that ETH meetup Friday?"),
        Message("19", "thread123", recipientOther, now - (35 * 60).seconds, now - (35 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, true, emptyList(), emptyList(), "Of course. Already polishing my Ledger and MetaMask hoodie 😎"),
        Message("20", "thread123", recipientMe, now - (34 * 60).seconds, now - (34 * 60).seconds, true, DeliveryStatus.PUBLISHED, null, false, emptyList(), emptyList(), "LFG 🔥")
    )
}
