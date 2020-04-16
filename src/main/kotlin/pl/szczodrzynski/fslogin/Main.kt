package pl.szczodrzynski.fslogin

import pl.szczodrzynski.fslogin.realm.AdfsLightRealm
import pl.szczodrzynski.fslogin.realm.AdfsRealm
import pl.szczodrzynski.fslogin.realm.BaseRealm
import pl.szczodrzynski.fslogin.realm.CufsRealm
import java.io.File

class Main(args: Array<String>) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main(args)
        }
    }

    init { run {
        println("FS Login")

        /*val realms2 = listOf(
            "https://uonetplus.vulcan.net.pl/default/LoginEndpoint.aspx",
            "https://uonetplus.vulcan.net.pl/powiatketrzynski/LoginEndpoint.aspx",
            "https://uonetplus.resman.pl/rzeszow/LoginEndpoint.aspx",
            "https://uonetplus.umt.tarnow.pl/tarnow/LoginEndpoint.aspx",
            "https://uonetplus.edu.lublin.eu/lublin/LoginEndpoint.aspx",
            "https://uonetplus.eszkola.opolskie.pl/default/LoginEndpoint.aspx",
            "https://uonetplus.edu.gdansk.pl/gdansk/LoginEndpoint.aspx",
            "https://iuczniowie.eduportal.koszalin.pl/"
        )*/

        val authType: String? = null
        val realms = listOf(
            // CUFS
            CufsRealm(host = "vulcan.net.pl", symbol = "default"),
            CufsRealm(host = "fakelog.cf", symbol = "powiatwulkanowy"),
            // ADFS
            CufsRealm(host = "umt.tarnow.pl", symbol = "tarnow").toAdfsRealm(id = "adfs", authType = authType),
            CufsRealm(host = "edu.gdansk.pl", symbol = "gdansk").toAdfsRealm(id = "adfs", authType = authType),
            CufsRealm(host = "eszkola.opolskie.pl", symbol = "opole", httpCufs = true).toAdfsRealm(id = "eSzkola", authType = authType),
            /* https://uonetplus.eszkola.opolskie.pl/brzeg/BrzegG1 */
            CufsRealm(host = "eszkola.opolskie.pl", symbol = "brzeg", httpCufs = true, realmPath = "brzegg1/LoginEndpoint.aspx").toAdfsRealm(id = "eSzkola", authType = authType),
            // ADFS LIGHT
            CufsRealm(host = "vulcan.net.pl", symbol = "powiatketrzynski").toAdfsLightRealm(id = "ADFSLight", isScoped = true),
            CufsRealm(host = "resman.pl", symbol = "rzeszow").toAdfsLightRealm(id = "ADFS"),
            CufsRealm(host = "edu.lublin.eu", symbol = "lublin").toAdfsLightRealm(id = "AdfsLight", domain = "logowanie"),
            // IUCZNIOWIE
            AdfsRealm(hostPrefix = "iuczniowie", host = "eduportal.koszalin.pl", path = "Default.aspx", id = "passive", authType = authType),
            AdfsRealm(hostPrefix = "iuczniowie", host = "eszkola.opolskie.pl", path = "Default.aspx", id = "passive", authType = authType),
            // LIBRUS SSO
            AdfsLightRealm(hostPrefix = "synergia", host = "librus.pl", adfsHost = "oswiatawradomiu.pl", path = "loguj/radom", id = "passive")
        )

        /*for (realm in realms) {
            println("URL for ${realm.getFinalRealm()} is: $realm")
        }*/

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

        FSLogin(realm, username, password, onSuccess = { cert ->
            println("Certificate:")
            println(" - title: ${cert?.pageTitle}")
            println(" - action: ${cert?.formAction}")
            println(" - wa: ${cert?.wa}")
            println(" - wresult: ${cert?.wresult?.take(100)}...")
            println(" - wctx: ${cert?.wctx}")

            println()
            print("Type filename to dump XML certificate, or Enter to skip: ")
            val filename = readLine()

            if (filename != null && filename.isNotBlank()) {
                val file = File(filename)
                file.createNewFile()
                file.writeText(cert?.wresult ?: "null")
                println("Saved as "+file.absolutePath)
            }
        }, onFailure = { errorText ->
            println("Incorrect credentials! ($errorText)")
            println()
            run(realm)
        })
    }
}
