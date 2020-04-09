package pl.szczodrzynski.fslogin

import java.net.URLDecoder
import java.net.URLEncoder

fun String.decode() = URLDecoder.decode(this, "UTF-8")
fun String.encode() = URLEncoder.encode(this, "UTF-8")

fun queryFrom(vararg params: Pair<String, String>): String {
    return params.joinToString("&") { it.first + "=" + it.second.encode() }
}
