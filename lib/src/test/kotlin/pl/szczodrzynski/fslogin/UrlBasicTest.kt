package pl.szczodrzynski.fslogin

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import pl.szczodrzynski.fslogin.realm.RealmData
import pl.szczodrzynski.fslogin.realm.toRealm

@RunWith(Parameterized::class)
class UrlBasicTest(
    testData: Pair<String, RealmData>
) {
    companion object {
        @Parameterized.Parameters
        @JvmStatic
        fun input(): List<Pair<String, RealmData>> {
            return listOf(
                "https://uonetplus.vulcan.net.pl/default/LoginEndpoint.aspx" to RealmData(
                    type = RealmData.Type.Cufs,
                    host = "vulcan.net.pl",
                    symbol = "default"
                ),
                "https://uonetplus.eszkola.opolskie.pl/default/LoginEndpoint.aspx" to RealmData(
                    type = RealmData.Type.CufsAdfs,
                    host = "eszkola.opolskie.pl",
                    symbol = "default",
                    adfsId = "eSzkola"
                ),
                "https://uonetplus.edu.gdansk.pl/gdansk/LoginEndpoint.aspx" to RealmData(
                    type = RealmData.Type.CufsAdfsLight,
                    host = "edu.gdansk.pl",
                    symbol = "gdansk",
                    adfsDomain = "logowanie",
                    adfsId = "AdfsLight"
                ),
                "https://uonetplus.resman.pl/rzeszow/LoginEndpoint.aspx" to RealmData(
                    type = RealmData.Type.CufsAdfsLight,
                    host = "resman.pl",
                    symbol = "rzeszow",
                    adfsId = "ADFS"
                ),
                "https://uonetplus.vulcan.net.pl/rzeszowprojekt/LoginEndpoint.aspx" to RealmData(
                    type = RealmData.Type.CufsAdfsLight,
                    host = "vulcan.net.pl",
                    symbol = "rzeszowprojekt",
                    adfsId = "AdfsLight",
                    adfsIsScoped = true
                ),
                "https://uonetplus.eduportal.koszalin.pl/koszalin/LoginEndpoint.aspx" to RealmData(
                    type = RealmData.Type.CufsAdfsPortal,
                    host = "eduportal.koszalin.pl",
                    symbol = "koszalin",
                    adfsId = "ADFS",
                    adfsPortalDomain = "EDUPORTAL"
                ),
                "https://uonetplus.umt.tarnow.pl/tarnow/LoginEndpoint.aspx" to RealmData(
                    type = RealmData.Type.CufsAdfsPortal,
                    host = "umt.tarnow.pl",
                    symbol = "tarnow",
                    adfsId = "adfs",
                    adfsPortalDomain = "EDUNET"
                ),
                "https://uonetplus.edu.lublin.eu/lublin/LoginEndpoint.aspx" to RealmData(
                    type = RealmData.Type.CufsAdfsLight,
                    host = "edu.lublin.eu",
                    symbol = "lublin",
                    adfsDomain = "logowanie",
                    adfsId = "AdfsLight"
                ),
                "https://uonetplus.vulcan.net.pl/powiatketrzynski/LoginEndpoint.aspx" to RealmData(
                    type = RealmData.Type.CufsAdfsLight,
                    host = "vulcan.net.pl",
                    symbol = "powiatketrzynski",
                    adfsIsScoped = true,
                    adfsId = "ADFSLight"
                )
            )
        }
    }

    private val url = testData.first
    private val data = testData.second

    private val http by lazy {
        OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.COMPATIBLE_TLS))
            .build()
    }

    @Test
    fun urlTest() {
        val startUrl = url
        val realm = data.toRealm()

        val request = Request.Builder()
            .url(startUrl)
            .build()
        val response = http.newCall(request).execute()
        val finalUrl = response.request().url()

        val urlEntityRegex = """%((?:25)*[0-9A-Fa-f]{2})""".toRegex()
        val dateTimeRegex = """\d{4}-\d{2}-\d{2}T(?:\d{2}%(?:25)*[0-9A-Fa-f]{2}){2}\d{2}Z""".toRegex()

        val expected = finalUrl
            .toString()
            .replace(urlEntityRegex) { it.value.toLowerCase() }
            .replace(dateTimeRegex, "DATETIME")
            .replace("Default.aspx", "default.aspx")
            .substringBefore("%26wct%3d")
            .substringBefore("&wct=")
        val actual = realm
            .toString()
            .replace(urlEntityRegex) { it.value.toLowerCase() }
            .replace(dateTimeRegex, "DATETIME")
            .substringBefore("%26wct%3d")
            .substringBefore("&wct=")

        println("Exptected: $expected")
        println("Actual:    $actual")

        assertEquals(expected, actual)
    }
}
