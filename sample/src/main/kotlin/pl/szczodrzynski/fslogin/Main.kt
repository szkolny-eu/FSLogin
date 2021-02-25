package pl.szczodrzynski.fslogin

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import pl.szczodrzynski.fslogin.realm.AdfsLightRealm
import pl.szczodrzynski.fslogin.realm.BaseRealm
import pl.szczodrzynski.fslogin.realm.toRealm
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class Main(args: Array<String>) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main(args)
        }
    }

    private val http by lazy {
        OkHttpClient.Builder()
                .cookieJar(MyCookieJar())
                .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.COMPATIBLE_TLS))
                .build()
    }

    private val realmsService = Retrofit.Builder()
        .client(http)
        .baseUrl("https://szkolny-eu.github.io/FSLogin/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RealmsService::class.java)

    init { run {
        println("FS Login")

        val authType: String? = null

        println("Getting FSLogin realms\n")
        val realmsResponse = realmsService.getRealms("vulcan").execute()
        val realmsRaw = realmsResponse.body() ?: throw RuntimeException(realmsResponse.message())

        val realms = realmsRaw
            .mapNotNull { it.realmData.toRealm() }
            .toMutableList()

        realms.add(
            // LIBRUS SSO
            AdfsLightRealm(hostPrefix = "synergia", host = "librus.pl", adfsHost = "oswiatawradomiu.pl", path = "loguj/radom", id = "passive")
        )

        realms.forEachIndexed { index, realm ->
            println(" - $index: ${realm.getFinalRealm()}")
        }

        print("Enter your choice: ")
        val choice = readLine()?.toIntOrNull() ?: return@run
        val realm = realms.getOrNull(choice) ?: return@run

        println("Realm Form URL:")
        println(realm.toString())
        println()

        run(realm)
    }}

    fun run(realm: BaseRealm) {
        print("Enter username/email: ")
        val username = readLine() ?: return

        print("Enter password: ")
        val password = readLine() ?: return

        val fsLogin = FSLogin(http, debug = true)

        fsLogin.performLogin(realm, username, password, onSuccess = { cert ->
            println("Certificate:")
            println(" - title: ${cert.pageTitle}")
            println(" - action: ${cert.formAction}")
            println(" - wa: ${cert.wa}")
            println(" - wresult: ${cert.wresult.take(100)}...")
            println(" - wctx: ${cert.wctx}")

            if (cert.formAction != realm.getFinalRealm()) {
                println("!!! Certificate action is different than the final realm:")
                println(cert.formAction)
                println("vs")
                println(realm.getFinalRealm())
            }

            /*val result = fsLogin.api.postCertificate(cert.formAction, mapOf(
                "wa" to cert.wa,
                "wresult" to cert.wresult,
                "wctx" to cert.wctx
            )).execute().body()

            print("POSTing the cert returned: \n")
            println(result)
            println()*/

            println()
            print("Type filename to dump XML certificate, or Enter to skip: ")
            val filename = readLine()

            if (filename != null && filename.isNotBlank()) {
                val file = File(filename)
                file.createNewFile()
                file.writeText(cert.wresult)
                println("Saved as " + file.absolutePath)
            }
        }, onFailure = { errorText ->
            println("Incorrect credentials! ($errorText)")
            println()
            run(realm)
        })
    }
}
