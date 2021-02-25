package pl.szczodrzynski.fslogin.realm

data class RealmData(
    val type: Type,
    val hostPrefix: String? = null,
    val host: String,
    val symbol: String = "default",
    val path: String? = null,
    val cufsHttp: Boolean = false,
    val adfsHost: String? = null,
    val adfsId: String? = null,
    val adfsDomain: String? = null,
    val adfsIsScoped: Boolean = false,
    val adfsPortalDomain: String? = null
) {

    enum class Type {
        Cufs,
        CufsAdfs,
        CufsAdfsPortal,
        CufsAdfsLight,
        Adfs,
        AdfsPortal,
        AdfsLight
    }
}
