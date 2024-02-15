package org.ethereumhpone.domain.model

data class UserData(
    // internal
    val canUseSubId: Boolean = true,
    val signature: String = "",
    val unicode: Boolean = false,
    val longAsMms: Boolean = false,
    val mmsSize: Int = 300,

    //
    val shouldHideOnboarding: Boolean,
    val darkThemeConfig: DarkThemeConfig,

)


enum class DarkThemeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}
