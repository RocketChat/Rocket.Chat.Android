package chat.rocket.android.helper

import android.app.Activity
import android.content.IntentSender
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import timber.log.Timber

const val REQUEST_CODE_FOR_SIGN_IN_REQUIRED = 1
const val REQUEST_CODE_FOR_MULTIPLE_ACCOUNTS_RESOLUTION = 2
const val REQUEST_CODE_FOR_SAVE_RESOLUTION = 3

/**
 * This class handles some cases of Google Smart Lock for passwords like the request to retrieve
 * credentials, to retrieve sign-in hints and to store the credentials.
 *
 * See https://developers.google.com/identity/smartlock-passwords/android/overview for futher
 * information.
 */
object SmartLockHelper {

    /**
     * Requests for stored Google Smart Lock credentials.
     * Note that in case of exception it will try to start a sign in
     * ([REQUEST_CODE_FOR_SIGN_IN_REQUIRED]) or "multiple account"
     * ([REQUEST_CODE_FOR_MULTIPLE_ACCOUNTS_RESOLUTION]) resolution.
     *
     * @param credentialsClient The credential client.
     * @param activity The activity.
     * @return null or the [Credential] result.
     */
    fun requestStoredCredentials(
        credentialsClient: CredentialsClient,
        activity: Activity
    ): Credential? {
        var credential: Credential? = null

        val credentialRequest = CredentialRequest.Builder()
            .setPasswordLoginSupported(true)
            .build()

        credentialsClient.request(credentialRequest)
            .addOnCompleteListener {
                when {
                    it.isSuccessful -> {
                        credential = it.result?.credential
                    }
                    it.exception is ResolvableApiException -> {
                        val resolvableApiException = (it.exception as ResolvableApiException)
                        if (resolvableApiException.statusCode == CommonStatusCodes.SIGN_IN_REQUIRED) {
                            provideSignInHint(credentialsClient, activity)
                        } else {
                            // This is most likely the case where the user has multiple saved
                            // credentials and needs to pick one. This requires showing UI to
                            // resolve the read request.
                            resolveResult(
                                resolvableApiException,
                                REQUEST_CODE_FOR_MULTIPLE_ACCOUNTS_RESOLUTION,
                                activity
                            )
                        }
                    }
                }
            }
        return credential
    }

    /**
     * Saves a user credential to Google Smart Lock.
     * Note that in case of exception it will try to start a save resolution,
     * so the activity/fragment should expected for a request code
     * ([REQUEST_CODE_FOR_SAVE_RESOLUTION]) on onActivityResult call.
     *
     * @param credentialsClient The credential client.
     * @param activity The activity.
     * @param id The user id credential.
     * @param password The user password credential.
     */
    fun save(
        credentialsClient: CredentialsClient,
        activity: FragmentActivity,
        id: String,
        password: String
    ) {
        val credential = Credential.Builder(id)
            .setPassword(password)
            .build()

        credentialsClient.save(credential)
            .addOnCompleteListener {
                val exception = it.exception
                if (exception is ResolvableApiException) {
                    // Try to resolve the save request. This will prompt the user if
                    // the credential is new.
                    try {
                        exception.startResolutionForResult(
                            activity,
                            REQUEST_CODE_FOR_SAVE_RESOLUTION
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Timber.e("Failed to send resolution. Exception is: $e")
                    }
                }
            }
    }

    private fun provideSignInHint(credentialsClient: CredentialsClient, activity: Activity) {
        val hintRequest = HintRequest.Builder()
            .setHintPickerConfig(
                CredentialPickerConfig.Builder()
                    .setShowCancelButton(true)
                    .build()
            )
            .setEmailAddressIdentifierSupported(true)
            .build()

        try {
            val intent = credentialsClient.getHintPickerIntent(hintRequest)
            activity.startIntentSenderForResult(
                intent.intentSender,
                REQUEST_CODE_FOR_SIGN_IN_REQUIRED,
                null,
                0,
                0,
                0,
                null
            )
        } catch (e: IntentSender.SendIntentException) {
            Timber.e("Could not start hint picker Intent. Exception is: $e")
        }
    }

    private fun resolveResult(
        exception: ResolvableApiException,
        requestCode: Int,
        activity: Activity
    ) {
        try {
            exception.startResolutionForResult(activity, requestCode)
        } catch (e: IntentSender.SendIntentException) {
            Timber.e("Failed to send resolution. Exception is: $e")
        }
    }
}