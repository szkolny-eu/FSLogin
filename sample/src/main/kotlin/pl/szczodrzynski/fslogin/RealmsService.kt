package pl.szczodrzynski.fslogin

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RealmsService {

    /**
     * Get all available FSLogin realms for the specified platform name.
     */
    @GET("realms/{platform}.json")
    fun getRealms(@Path("platform") platformName: String): Call<List<RealmInfo>>
}
