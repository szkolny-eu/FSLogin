package pl.szczodrzynski.fslogin.realm

object RealmUtils {

    private fun dataToCufs(data: RealmData): CufsRealm {
        assert(data.type.name.startsWith("Cufs"))
        return CufsRealm(
            httpCufs = data.cufsHttp,
            host = data.host,
            symbol = data.symbol,
            realmPath =
            if (data.type.name.contains("Adfs") && data.adfsDomain == null && !data.adfsIsScoped)
                ""
            else
                "LoginEndpoint.aspx"
        )
    }

    fun fromData(data: RealmData): BaseRealm? {
        return when (data.type) {
            RealmData.Type.Cufs -> dataToCufs(data)
            RealmData.Type.CufsAdfs -> dataToCufs(data).toAdfsRealm(
                id = data.adfsId ?: "adfs"
            )
            RealmData.Type.CufsAdfsPortal -> dataToCufs(data).toAdfsPortalRealm(
                id = data.adfsId ?: "adfs",
                portalDomain = data.adfsPortalDomain ?: ""
            )
            RealmData.Type.CufsAdfsLight -> dataToCufs(data).toAdfsLightRealm(
                id = data.adfsId ?: "adfs",
                domain = data.adfsDomain ?: "adfslight",
                isScoped = data.adfsIsScoped
            )
            RealmData.Type.Adfs -> AdfsRealm(
                hostPrefix = data.hostPrefix ?: return null,
                host = data.host,
                adfsHost = data.adfsHost ?: data.host,
                path = data.path ?: "Default.aspx",
                id = data.adfsId ?: "adfs"
            )
            RealmData.Type.AdfsPortal -> AdfsPortalRealm(
                hostPrefix = data.hostPrefix ?: return null,
                host = data.host,
                adfsHost = data.adfsHost ?: data.host,
                path = data.path ?: "Default.aspx",
                id = data.adfsId ?: "adfs",
                portalDomain = data.adfsPortalDomain ?: ""
            )
            RealmData.Type.AdfsLight -> AdfsLightRealm(
                hostPrefix = data.hostPrefix ?: return null,
                host = data.host,
                adfsHost = data.adfsHost ?: data.host,
                path = data.path ?: "Default.aspx",
                id = data.adfsId ?: "adfs",
                domain = data.adfsDomain ?: "adfslight"
            )
        }
    }

    fun toData(realm: BaseRealm): RealmData? {
        return when (realm) {
            is CufsRealm -> RealmData(
                type = RealmData.Type.Cufs,
                host = realm.host,
                symbol = realm.symbol,
                cufsHttp = realm.httpCufs
            )
            is AdfsLightRealm -> when (realm.cufsRealm) {
                null -> RealmData(
                    type = RealmData.Type.AdfsLight,
                    hostPrefix = realm.hostPrefix,
                    host = realm.host,
                    adfsHost = realm.adfsHost,
                    path = realm.path,
                    adfsId = realm.id,
                    adfsDomain = realm.domain
                )
                else -> RealmData(
                    type = RealmData.Type.CufsAdfsLight,
                    host = realm.host,
                    symbol = realm.cufsRealm.symbol,
                    cufsHttp = realm.cufsRealm.httpCufs,
                    adfsId = realm.id,
                    adfsDomain = realm.domain,
                    adfsIsScoped = realm.scope.isNotBlank()
                )
            }
            is AdfsPortalRealm -> when (realm.cufsRealm) {
                null -> RealmData(
                    type = RealmData.Type.AdfsPortal,
                    hostPrefix = realm.hostPrefix,
                    host = realm.host,
                    adfsHost = realm.adfsHost,
                    path = realm.path,
                    adfsId = realm.id,
                    adfsPortalDomain = realm.portalDomain
                )
                else -> RealmData(
                    type = RealmData.Type.CufsAdfsPortal,
                    host = realm.host,
                    symbol = realm.cufsRealm.symbol,
                    cufsHttp = realm.cufsRealm.httpCufs,
                    adfsId = realm.id,
                    adfsPortalDomain = realm.portalDomain
                )
            }
            is AdfsRealm -> when (realm.cufsRealm) {
                null -> RealmData(
                    type = RealmData.Type.Adfs,
                    hostPrefix = realm.hostPrefix,
                    host = realm.host,
                    adfsHost = realm.adfsHost,
                    path = realm.path,
                    adfsId = realm.id
                )
                else -> RealmData(
                    type = RealmData.Type.CufsAdfs,
                    host = realm.host,
                    symbol = realm.cufsRealm.symbol,
                    cufsHttp = realm.cufsRealm.httpCufs,
                    adfsId = realm.id
                )
            }
            else -> null
        }
    }
}

fun RealmData.toRealm(): BaseRealm? = RealmUtils.fromData(this)
fun BaseRealm.toData(): RealmData? = RealmUtils.toData(this)
