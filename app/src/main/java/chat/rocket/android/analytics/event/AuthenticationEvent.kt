package chat.rocket.android.analytics.event

sealed class AuthenticationEvent(val methodName: String) {

    object AuthenticationWithUserAndPassword : AuthenticationEvent("User and password")
    object AuthenticationWithCas : AuthenticationEvent("CAS")
    object AuthenticationWithSaml : AuthenticationEvent("SAML")
    object AuthenticationWithOauth : AuthenticationEvent("Oauth")
    object AuthenticationWithDeeplink : AuthenticationEvent("Deep link")
}

