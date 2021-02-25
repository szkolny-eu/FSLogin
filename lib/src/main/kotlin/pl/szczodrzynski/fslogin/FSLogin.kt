package pl.szczodrzynski.fslogin

import okhttp3.OkHttpClient
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import pl.szczodrzynski.fslogin.realm.BaseRealm
import pl.szczodrzynski.fslogin.response.FSCertificateResponse
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class FSLogin(
    private val http: OkHttpClient,
    val debug: Boolean = false
) {
    private val api by lazy {
        val retrofit = Retrofit.Builder()
            .client(http)
            .baseUrl("https://example.com/") // retrofit needs a base URL
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(JspoonConverterFactory.create())
            .build()
        retrofit.create(FSService::class.java)
    }

    /**
     * Perform a login operation on specified [realm] using
     * the given [username] (might be an e-mail address) and
     * [password].
     *
     * The generated certificate may be later sent to [BaseRealm.getFinalRealm]
     * (for example using [api]) to complete the login process.
     *
     * @param onSuccess executed with a certificate as parameter
     * @param onFailure executed with the error text as parameter
     */
    fun performLogin(
        realm: BaseRealm,
        username: String,
        password: String,
        onSuccess: (cert: FSCertificateResponse) -> Unit,
        onFailure: (errorText: String) -> Unit
    ) {
        if (debug) println("Getting realm $realm")
        val certificate = realm.getCertificate(api, username, password)
        when {
            certificate.isValid -> {
                onSuccess(certificate)
            }
            else -> {
                val errorText = when {
                    certificate.errorTextCufs.isNotBlank() -> certificate.errorTextCufs
                    certificate.errorTextAdfs.isNotBlank() -> certificate.errorTextAdfs
                    certificate.errorTextAdfsPortal.isNotBlank() -> certificate.errorTextAdfsPortal
                    else -> "The returned webpage does not look correct"
                }
                onFailure(errorText)
            }
        }
    }
}
