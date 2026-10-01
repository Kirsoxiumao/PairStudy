package com.pairstudy.app.data.api
import com.pairstudy.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*
interface ApiService {
    @POST("api/auth/login") suspend fun login(@Body body: LoginInput): ApiResponse<Auth>
    @POST("api/auth/register") suspend fun register(@Body body: RegisterInput): ApiResponse<Auth>
    @GET("api/pair/info") suspend fun pair(): ApiResponse<PairInfo>
    @POST("api/pair/create-invite") suspend fun invite(): ApiResponse<PairInfo>
    @POST("api/pair/bind") suspend fun bind(@Body body: BindInput): ApiResponse<PairInfo>
    @GET("api/categories") suspend fun categories(): ApiResponse<List<Category>>
    @POST("api/categories") suspend fun createCategory(@Body body: CategoryInput): ApiResponse<Category>
    @PUT("api/categories/{id}") suspend fun updateCategory(@Path("id") id: Long, @Body body: CategoryInput): ApiResponse<Category>
    @DELETE("api/categories/{id}") suspend fun archiveCategory(@Path("id") id: Long): ApiResponse<Unit>
    @Multipart @POST("api/upload/image") suspend fun upload(@Part file: MultipartBody.Part): ApiResponse<Upload>
    @POST("api/checkins") suspend fun createCheckin(@Body body: CheckinInput): ApiResponse<Checkin>
    @GET("api/checkins/feed") suspend fun feed(@Query("page") page: Int, @Query("size") size: Int = 20): ApiResponse<Page<Checkin>>
    @GET("api/checkins/date/{date}") suspend fun day(@Path("date") date: String, @Query("page") page: Int, @Query("author") author: String = "all"): ApiResponse<Page<Checkin>>
    @GET("api/checkins/category/{id}") suspend fun categoryFeed(@Path("id") id: Long, @Query("author") author: String, @Query("page") page: Int): ApiResponse<Page<Checkin>>
    @DELETE("api/checkins/{id}") suspend fun deleteCheckin(@Path("id") id: Long): ApiResponse<Unit>
    @GET("api/calendar/month") suspend fun month(@Query("year") year: Int, @Query("month") month: Int): ApiResponse<List<CalendarDay>>
    @GET("api/profile") suspend fun profile(): ApiResponse<Profile>
    @GET("api/profile/statistics") suspend fun statistics(): ApiResponse<Statistics>
}
