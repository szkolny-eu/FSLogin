package pl.szczodrzynski.fslogin

import pl.szczodrzynski.fslogin.response.ADFSFormResponse
import pl.szczodrzynski.fslogin.response.FSCertificateResponse
import retrofit2.Call
import retrofit2.http.*

interface FSService {

    /**
     * Make a POST request to the [url] and return all ASP.NET form fields
     * which need to be sent with the next request.
     */
    @POST
    @FormUrlEncoded
    fun getAdfsForm(@Url url: String, @Field("__db") db: Int = 15): Call<ADFSFormResponse>

    @POST
    @FormUrlEncoded
    fun postCredentials(@Url url: String, @FieldMap formFields: Map<String, String>): Call<FSCertificateResponse>

    @POST
    @FormUrlEncoded
    fun postCertificate(@Url url: String, @FieldMap formFields: Map<String, String>): Call<String>
}
