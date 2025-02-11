package com.verve.emovision.data.source.network.service

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiServices {
//    @POST("auth/register")
//    @Headers("Content-Type: application/json")
//    suspend fun doRegister(
//        @Body registerRequest: RegisterRequestResponse,
//    ): Response<RegisterItemResponse>
//
//    @POST("auth/verify")
//    @Headers("Content-Type: application/json")
//    suspend fun doVerify(
//        @Body verifyRequest: VerifyRequestResponse,
//    ): VerifyResponse
//
//    @POST("auth/login")
//    @Headers("Content-Type: application/json")
//    suspend fun doLogin(
//        @Body loginRequest: LoginRequestResponse,
//    ): Response<LoginItemResponse>
//
//    @POST("auth/resend-otp")
//    @Headers("Content-Type: application/json")
//    suspend fun doResendOtp(
//        @Body resendCodeRequest: ResendOtpRequestResponse,
//    ): ResendOtpResponse
//
//    @GET("nutritionist/get")
//    suspend fun getNutritionists(
//        @Query("search") search: String? = null,
//    ): Response<List<NutritionistItemResponse>>
//
//    @GET("category/list")
//    suspend fun getCategories(): CategoriesResponse
//
//    @GET("menu")
//    suspend fun getFood(
//        @Query("category_id") category_id: String? = null,
//        @Query("search") search: String? = null,
//    ): FoodResponse
//
//    @GET("food")
//    suspend fun getFoodData(
//        @Query("food_id") foodId: String,
//    ): Response<FoodItemResponse>
//
//    @POST("order")
//    @Headers("Content-Type: application/json")
//    suspend fun createOrder(
//        @Body payload: CheckoutRequestPayload,
//    ): CheckoutResponse
//
//    @GET("order/pending")
//    suspend fun getOrders(): Response<List<OrderDataResponse>>
//
//    @POST("order/add")
//    @Headers("Content-Type: application/json")
//    suspend fun createOrderLocation(
//        @Body orderRequest: OrderRequestResponse,
//    ): Response<OrderDataResponse>
//
//    @GET("order")
//    suspend fun getOrdersByUserId(
//        @Query("user_id") userId: String,
//    ): Response<List<OrderDataResponse>>
//
//    @GET("order/items")
//    suspend fun getOrderItemsByOrderId(
//        @Query("order_id") orderId: String? = null,
//    ): Response<List<OrderItemResponse>>
//
//    @POST("order/on_delivery")
//    suspend fun changeOrderStatus(
//        @Query("order_id") orderId: String? = null,
//    ): Response<OrderDataResponse>
//
//    @GET("user")
//    suspend fun getUserData(
//        @Query("user_id") userID: String,
//    ): UserResponse
//
//    @Multipart
//    @POST("update_user")
//    suspend fun updateUserData(
//        @Query("id") id: String,
//        @Part("name") name: RequestBody,
//        @Part("email") email: RequestBody,
//        @Part("phone_number") phoneNumber: RequestBody,
//        @Part("address") address: RequestBody,
//    ): Response<UserItemResponse>
//
//    @DELETE("delete_user")
//    suspend fun deleteUser(
//        @Query("id") userId: String? = null,
//    ): DeleteUserResponse
//
//    @POST("chats/add")
//    suspend fun createChat(
//        @Body chatRequest: ChatRequest,
//    ): Response<ChatResponse>
//
//    @GET("chats/get")
//    suspend fun getChatData(
//        @Query("receiver_id") userID: String,
//    ): SelectChatResponse
//
//    @GET("chats")
//    suspend fun getChatsForUser(
//        @Query("user_id") userId: String,
//    ): Response<List<ChatResponse>>
//
//    @POST("messages/send")
//    suspend fun sendMessage(
//        @Body messageRequest: MessageRequest,
//    ): Response<MessageResponse>
//
//    @GET("messages")
//    suspend fun getMessagesForChat(
//        @Query("chat_id") chatId: String,
//    ): Response<List<MessageResponse>>

//    companion object {
//        @JvmStatic
//        operator fun invoke(): ApiServices {
//            val okHttpClient =
//                OkHttpClient.Builder()
//                    .connectTimeout(120, TimeUnit.SECONDS)
//                    .readTimeout(120, TimeUnit.SECONDS)
//                    .build()
//            val retrofit =
//                Retrofit.Builder()
//                    .baseUrl(BuildConfig.BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(okHttpClient)
//                    .build()
//            return retrofit.create(ApiServices::class.java)
//        }
//    }
}
