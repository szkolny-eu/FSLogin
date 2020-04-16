package pl.szczodrzynski.fslogin

import okhttp3.ConnectionSpec
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import pl.szczodrzynski.fslogin.realm.BaseRealm
import pl.szczodrzynski.fslogin.response.FSCertificateResponse
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import pl.szczodrzynski.fslogin.MyCookieJar

class FSLogin(
    val realm: BaseRealm,
    val username: String,
    val password: String,
    val onSuccess: (cert: FSCertificateResponse?) -> Unit,
    val onFailure: (errorText: String) -> Unit
) {
    val http by lazy {
        OkHttpClient.Builder()
            //.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("192.168.0.150", 8080)))
            .cookieJar(MyCookieJar())
            .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS))
            .build()
    }
    val httpLazy by lazy {
        http.newBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }
    val fs by lazy {
        val retrofit = Retrofit.Builder()
            .client(http)
            .baseUrl("https://${realm.getRealmHost()}/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(JspoonConverterFactory.create())
            .build()
        retrofit.create(FSService::class.java)
    }

    init {
        println("Getting realm $realm")
        val certificate = realm.getCertificate(fs, username, password)
        if (certificate?.pageTitle?.startsWith("Working...") == true) {
            onSuccess(certificate)
        }
        else if (certificate != null) {
            val errorText = if (certificate.errorTextCufs.isNotBlank())
                certificate.errorTextCufs
            else if (certificate.errorTextAdfs.isNotBlank())
                certificate.errorTextAdfs
            else
                "The returned webpage does not look correct"
            onFailure(errorText)
        }
        else {
            onFailure("Could not get any valid certificate")
        }
    }
}
