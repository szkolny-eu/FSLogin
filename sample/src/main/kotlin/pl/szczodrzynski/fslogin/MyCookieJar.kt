package pl.szczodrzynski.fslogin

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.ArrayList

class MyCookieJar : CookieJar {
    private var cookies: List<Cookie> = listOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies
    }
}
