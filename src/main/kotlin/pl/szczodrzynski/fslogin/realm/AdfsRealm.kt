package pl.szczodrzynski.fslogin.realm

import okhttp3.OkHttpClient
import pl.szczodrzynski.fslogin.FSService
import pl.szczodrzynski.fslogin.queryFrom
import pl.szczodrzynski.fslogin.response.FSCertificateResponse
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

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
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        format.timeZone = TimeZone.getTimeZone("UTC")
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
            "wct" to format.format(Date()).replace(" ", "T") + "Z"
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

    override fun getCertificate(fs: FSService, username: String, password: String): FSCertificateResponse? {
        var certificate = if (this is AdfsLightRealm) {
            fs.postCredentials(toString(), mapOf(
                "Username" to username,
                "Password" to password
            )).execute().body()
        }
        else {
            val adfsForm = fs.getAdfsForm(toString()).execute().body() ?: throw RuntimeException("adfsForm == null")

            fs.postCredentials(
                toString(), mapOf(
                    "__VIEWSTATE" to adfsForm.viewState,
                    "__VIEWSTATEGENERATOR" to adfsForm.viewStateGenerator,
                    "__EVENTVALIDATION" to adfsForm.eventValidation,
                    "__db" to "15",
                    "UsernameTextBox" to username,
                    "PasswordTextBox" to password,
                    "SubmitButton.x" to "0",
                    "SubmitButton.y" to "0"
                )
            ).execute().body()
        }

        if (certificate?.pageTitle?.startsWith("Working...") != true)
            return certificate
        println("Got certificate for ${certificate.formAction}")

        if (cufsRealm != null && certificate.formAction != getFinalRealm()) {
            certificate = fs.postCredentials(certificate.formAction, mapOf(
                "wa" to certificate.wa,
                "wresult" to certificate.wresult,
                "wctx" to certificate.wctx
            )).execute().body()
            if (certificate?.pageTitle?.startsWith("Working...") != true)
                return certificate
            println("Got certificate for ${certificate.formAction}")
        }

        return certificate
    }
}
