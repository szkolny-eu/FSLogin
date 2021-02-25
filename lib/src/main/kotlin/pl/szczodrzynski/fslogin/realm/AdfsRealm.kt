package pl.szczodrzynski.fslogin.realm

import pl.szczodrzynski.fslogin.FSService
import pl.szczodrzynski.fslogin.getTimestamp
import pl.szczodrzynski.fslogin.postCredentials
import pl.szczodrzynski.fslogin.queryFrom
import pl.szczodrzynski.fslogin.response.FSCertificateResponse

open class AdfsRealm(
    val scheme: String = "https",
    val hostPrefix: String,
    val host: String,
    val adfsHost: String = host,
    val path: String,
    val realmPath: String = path,
    val cufsRealm: CufsRealm? = null,
    val id: String,
    val authType: String? = null
) : BaseRealm {

    override fun toString(): String {
        val authPath = when (authType) {
            "integrated" -> "auth/integrated/" // __db=16
            "sslclient" -> "auth/sslclient/" // __db=17
            "basic" -> "auth/basic/" // __db=18
            else -> ""
        }
        return "$scheme://adfs.$adfsHost/adfs/ls/$authPath?" + queryFrom(
            "wa" to "wsignin1.0",
            "wtrealm" to getRealm(),
            "wctx" to getCtx(),
            "wct" to getTimestamp()
        )
    }

    private fun getReturnPath(): String {
        if (cufsRealm == null) {
            return path
        }
        return "$path?" + queryFrom(
            "wa" to "wsignin1.0",
            "wtrealm" to cufsRealm.getRealm(),
            "wctx" to cufsRealm.getCtx()
        )
    }

    override fun getRealmHost() = host
    override fun getRealm(): String {
        if (cufsRealm?.httpCufs == true) {
            return "http://$hostPrefix.$host/$realmPath"
        }
        if (cufsRealm != null) {
            return "$scheme://$hostPrefix.$host:443/$realmPath"
        }
        return "$scheme://$hostPrefix.$host/$realmPath"
    }
    override fun getCtx(): String {
        return queryFrom(
            "rm" to "0",
            "id" to id,
            "ru" to "/" + getReturnPath()
        )
    }
    override fun getFinalRealm() = cufsRealm?.getRealm() ?: getRealm()

    override fun getCertificate(
        fs: FSService,
        username: String,
        password: String,
        debug: Boolean
    ): FSCertificateResponse? {
        val adfsForm = fs.getAdfsForm(toString()).execute().body() ?: throw RuntimeException("adfsForm == null")

        val certificate = postCredentials(
            fs, toString(), mapOf(
                "__VIEWSTATE" to adfsForm.viewState,
                "__VIEWSTATEGENERATOR" to adfsForm.viewStateGenerator,
                "__EVENTVALIDATION" to adfsForm.eventValidation,
                "__db" to "15",
                "UsernameTextBox" to username,
                "PasswordTextBox" to password,
                "SubmitButton.x" to "0",
                "SubmitButton.y" to "0"
            ), debug
        )

        if (cufsRealm != null && certificate.formAction != getFinalRealm())
            return cufsRealm.getCertificate(fs, certificate, debug)

        return certificate
    }
}
