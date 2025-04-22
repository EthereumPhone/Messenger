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
