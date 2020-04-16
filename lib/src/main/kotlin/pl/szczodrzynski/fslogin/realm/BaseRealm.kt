package pl.szczodrzynski.fslogin.realm

import pl.szczodrzynski.fslogin.FSService
import pl.szczodrzynski.fslogin.response.FSCertificateResponse

interface BaseRealm {
    fun getRealmHost(): String
    fun getRealm(): String
    fun getCtx(): String
    fun getFinalRealm(): String

    /**
     * Perform a login operation.
     * @return a certificate meant to be POSTed to [getFinalRealm] URL.
     */
    fun getCertificate(fs: FSService, username: String, password: String, debug: Boolean = false): FSCertificateResponse?
}
