package pl.szczodrzynski.fslogin

import pl.droidsonroids.jspoon.Jspoon
import pl.szczodrzynski.fslogin.response.FSCertificateResponse
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun String.decode() = URLDecoder.decode(this, "UTF-8")
fun String.encode() = URLEncoder.encode(this, "UTF-8")

fun queryFrom(vararg params: Pair<String, String>): String {
    return params.joinToString("&") { it.first + "=" + it.second.encode() }
}

fun postCredentials(fs: FSService, url: String, params: Map<String, String>, debug: Boolean): FSCertificateResponse {
    val html = fs.postCredentials(url, params).execute().body()
    if (debug) println(html)
    val certificate = Jspoon.create().adapter(FSCertificateResponse::class.java).fromHtml(html ?: "")
    if (debug) println("Got certificate for ${certificate.formAction}")
    return certificate
}

fun getTimestamp(): String {
    return ZonedDateTime.now(ZoneId.of("UTC"))
        .format(DateTimeFormatter.ISO_INSTANT)
        .substringBefore(".") + "Z"
}
