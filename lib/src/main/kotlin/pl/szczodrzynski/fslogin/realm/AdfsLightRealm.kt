package pl.szczodrzynski.fslogin.realm

import pl.szczodrzynski.fslogin.*
import pl.szczodrzynski.fslogin.response.FSCertificateResponse

class AdfsLightRealm(
    scheme: String = "https",
    hostPrefix: String,
    host: String,
    adfsHost: String = host,
    path: String,
    realmPath: String = path,
    cufsRealm: CufsRealm? = null,
    id: String,
    val domain: String = "adfslight",
    val scope: String = ""
) : AdfsRealm(scheme, hostPrefix, host, adfsHost, path, realmPath, cufsRealm, id, null) {

    override fun toString(): String {
        // for scoped CUFS (with ADFSLight)
        val returnScope = if (scope.isNotBlank())
            "${scope.encode()}default.aspx"
        else ""

        return "$scheme://$domain.$adfsHost/${scope}LoginPage.aspx?ReturnUrl=%2F$returnScope%3F" + queryFrom(
            "wa" to "wsignin1.0",
            "wtrealm" to getRealm(),
            "wctx" to getCtx(),
            "wct" to getTimestamp()
        ).encode()
    }

    override fun getCertificate(
        fs: FSService,
        username: String,
        password: String,
        debug: Boolean
    ): FSCertificateResponse {
        val certificate = postCredentials(
            fs, toString(), mapOf(
                "Username" to username,
                "Password" to password
            ), debug
        )

        if (!certificate.isValid)
            return certificate

        if (cufsRealm != null && certificate.formAction != getFinalRealm())
            return cufsRealm.getCertificate(fs, certificate, debug)

        return certificate
    }
}
