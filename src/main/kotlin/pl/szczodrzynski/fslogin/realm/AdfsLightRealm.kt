package pl.szczodrzynski.fslogin.realm

import pl.szczodrzynski.fslogin.FSService
import pl.szczodrzynski.fslogin.encode
import pl.szczodrzynski.fslogin.queryFrom
import pl.szczodrzynski.fslogin.response.FSCertificateResponse
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

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
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        format.timeZone = TimeZone.getTimeZone("UTC")

        // for scoped CUFS (with ADFSLight)
        val returnScope = if (scope.isNotBlank())
            "${scope.encode()}default.aspx"
        else ""

        return "$scheme://$domain.$adfsHost/${scope}LoginPage.aspx?ReturnUrl=%2F$returnScope%3F" + queryFrom(
            "wa" to "wsignin1.0",
            "wtrealm" to getRealm(),
            "wctx" to getCtx(),
            "wct" to format.format(Date()).replace(" ", "T") + "Z"
        ).encode()
    }
}
