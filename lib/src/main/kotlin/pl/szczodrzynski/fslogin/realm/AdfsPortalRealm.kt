package pl.szczodrzynski.fslogin.realm

import pl.szczodrzynski.fslogin.FSService
import pl.szczodrzynski.fslogin.getTimestamp
import pl.szczodrzynski.fslogin.postCredentials
import pl.szczodrzynski.fslogin.queryFrom
import pl.szczodrzynski.fslogin.response.FSCertificateResponse

class AdfsPortalRealm(
    scheme: String = "https",
    hostPrefix: String,
    host: String,
    adfsHost: String = host,
    path: String,
    realmPath: String = path,
    cufsRealm: CufsRealm? = null,
    id: String,
    authType: String? = null,
    val portalDomain: String
) : AdfsRealm(scheme, hostPrefix, host, adfsHost, path, realmPath, cufsRealm, id, authType) {

    override fun toString(): String {
        val authPath = when (authType) {
            "integrated" -> "auth/integrated/" // __db=16
            "sslclient" -> "auth/sslclient/" // __db=17
            "basic" -> "auth/basic/" // __db=18
            else -> ""
        }
        return "$scheme://adfs.$adfsHost/adfs/ls$authPath?" + queryFrom(
            "wa" to "wsignin1.0",
            "wtrealm" to getRealm(),
            "wctx" to getCtx(),
            "wct" to getTimestamp()
        )
    }

    override fun getCertificate(
        fs: FSService,
        username: String,
        password: String,
        debug: Boolean
    ): FSCertificateResponse? {
        val certificate = postCredentials(
            fs, toString(), mapOf(
                "UserName" to "$portalDomain\\$username",
                "Password" to password,
                "AuthMethod" to "FormsAuthentication"
            ), debug
        )

        if (cufsRealm != null && certificate.formAction != getFinalRealm())
            return cufsRealm.getCertificate(fs, certificate, debug)

        return certificate
    }
}
