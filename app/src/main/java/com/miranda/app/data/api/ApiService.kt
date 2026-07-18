package com.miranda.app.data.api

import com.miranda.app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<UserDataSimple>>

    @GET("api/dashboard")
    suspend fun getDashboard(): Response<ApiResponse<DashboardResponse>>

    @GET("api/tariffs")
    suspend fun getTariffs(): Response<ApiResponse<List<TariffData>>>

    @PATCH("api/tariffs/select/{id}")
    suspend fun selectTariff(@Path("id") tariffId: Int): Response<ApiResponse<TariffSelectionResponse>>

    // Добавьте в интерфейс ApiService:

    @GET("api/services")
    suspend fun getServices(): Response<ApiResponse<List<ServiceData>>>

    @PATCH("api/services/toggle/{id}")
    suspend fun toggleService(
        @Path("id") serviceId: Int,
        @Body body: ToggleServiceRequest
    ): Response<ApiResponse<ServiceToggleResponse>>
}