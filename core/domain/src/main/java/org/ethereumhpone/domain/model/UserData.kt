package org.ethereumhpone.domain.model

data class UserData(
    // internal
    val canUseSubId: Boolean = true,
    val signature: String = "",
    val unicode: Boolean = false,
    val longAsMms: Boolean = false,
    val mmsSize: Int = 300,
    val delivery: Boolean = false,

    // external
    val shouldHideOnboarding: Boolean = false,
    val darkThemeConfig: DarkThemeConfig,
    val threadNotificationsId: Map<String, Boolean>,
    val ringTone: String,
    val useXmtp: Boolean = false

)


enum class DarkThemeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}
