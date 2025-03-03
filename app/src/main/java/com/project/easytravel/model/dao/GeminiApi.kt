import com.project.easytravel.BuildConfig
import com.project.easytravel.base.GeminiRequest
import com.project.easytravel.base.GeminiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    fun getTravelPlan(
        @Query("key") apiKey: String = com.project.easytravel.BuildConfig.GEMINI_API_KEY,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}
