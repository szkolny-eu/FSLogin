package pl.szczodrzynski.fslogin.realm

import pl.szczodrzynski.fslogin.FSService
import pl.szczodrzynski.fslogin.encode
import pl.szczodrzynski.fslogin.postCredentials
import pl.szczodrzynski.fslogin.queryFrom
import pl.szczodrzynski.fslogin.response.FSCertificateResponse

class CufsRealm(
    val scheme: String = "https",
    val httpCufs: Boolean = false,
    val host: String,
    val symbol: String = "default",
    val path: String = "FS/LS",
    val realmPath: String = "LoginEndpoint.aspx"
) : BaseRealm {

    override fun toString(): String {
        return "$scheme://cufs.$host/$symbol/Account/LogOn?ReturnUrl=%2F"+getReturnPath().encode()
    }

    private fun getReturnPath(): String {
        return "$symbol/$path?" + queryFrom(
            "wa" to "wsignin1.0",
            "wtrealm" to getRealm(),
            "wctx" to getCtx()
        )
    }

    override fun getRealmHost() = host
    override fun getRealm() = "$scheme://uonetplus.$host/$symbol/$realmPath"
    override fun getCtx() = "$scheme://uonetplus.$host/$symbol/$realmPath"
    override fun getFinalRealm() = getRealm()
    override fun getCertificate(fs: FSService, username: String, password: String, debug: Boolean): FSCertificateResponse? {
        return postCredentials(
            fs, toString(), mapOf(
                "LoginName" to username,
                "Password" to password
            ), debug
        )
    }

    fun getCertificate(fs: FSService, adfsCertificate: FSCertificateResponse, debug: Boolean): FSCertificateResponse {
        return postCredentials(
            fs, adfsCertificate.formAction, mapOf(
                "wa" to adfsCertificate.wa,
                "wresult" to adfsCertificate.wresult,
                "wctx" to adfsCertificate.wctx
            ), debug
        )
    }

    fun toAdfsRealm(id: String, authType: String? = null): AdfsRealm {
        return AdfsRealm(
            scheme = scheme,
            hostPrefix = "cufs",
            host = host,
            path = "$symbol/FS/LS",
            realmPath = "$symbol/Account/LogOn",
            cufsRealm = this,
            id = id,
            authType = authType
        )
    }

    fun toAdfsPortalRealm(id: String, portalDomain: String, authType: String? = null): AdfsRealm {
        return AdfsPortalRealm(
            scheme = scheme,
            hostPrefix = "cufs",
            host = host,
            path = "$symbol/FS/LS",
            realmPath = "$symbol/Account/LogOn",
            cufsRealm = this,
            id = id,
            authType = authType,
            portalDomain = portalDomain
        )
    }

    fun toAdfsLightRealm(id: String, domain: String = "adfslight", isScoped: Boolean = false): AdfsRealm {
        return AdfsLightRealm(
            scheme = scheme,
            hostPrefix = "cufs",
            host = host,
            path = "$symbol/FS/LS",
            realmPath = "$symbol/Account/LogOn",
            cufsRealm = this,
            id = id,
            domain = domain,
            scope = if (isScoped) "$symbol/" else ""
        )
    }
}
