package pl.szczodrzynski.fslogin;

import java.util.ArrayList;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

class MyCookieJar : CookieJar {
    private var cookies: List<Cookie>? = null
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie>? {
        if(cookies != null)
            return cookies
        else
            return ArrayList()
    }
}