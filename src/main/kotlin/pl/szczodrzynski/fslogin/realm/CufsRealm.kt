package pl.szczodrzynski.fslogin.realm

import pl.szczodrzynski.fslogin.FSService
import pl.szczodrzynski.fslogin.encode
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
    override fun getCertificate(fs: FSService, username: String, password: String): FSCertificateResponse? {
        val certificate = fs.postCredentials(toString(), mapOf(
            "LoginName" to username,
            "Password" to password
        )).execute().body()
        if (certificate?.pageTitle?.startsWith("Working...") != true)
            return null
        println("Got certificate for ${certificate.formAction}")
        return certificate
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
