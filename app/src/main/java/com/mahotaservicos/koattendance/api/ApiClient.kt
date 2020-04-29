package com.mahotaservicos.koattendance.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.concurrent.TimeUnit


object  ApiClient {
    val BASE_URL_Nexmo = "https://rest.nexmo.com/"
    private lateinit var retrofit: Retrofit

    fun getClient(): Retrofit {
        //Increase the default timeout time
        val client = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS).build()
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL_Nexmo)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
        return retrofit
    }
}

internal interface ApiInterface {
    @FormUrlEncoded
    @POST("sms/json")
    fun getMessageResponse(
            @Field("api_key") apiKey: String?,
            @Field("api_secret") apiSecret: String?,
            @Field("from") from: String?,
            @Field("to") to: String?,
            @Field("text") text: String?
    ): Call<MessageResponse>
}

internal class MessageResponse {
    @SerializedName("message-count")
    var messageCount: String? = null

    @SerializedName("messages")
    private lateinit var messages: Array<Message>

    fun getMessages(): Array<Message> {
        return messages
    }

    fun setMessages(messages: Array<Message>) {
        this.messages = messages
    }

    override fun toString(): String {
        return "ClassPojo [message-count = $messageCount, messages = $messages]"
    }
}

internal class Message {
    @SerializedName("to")
    var to: String? = null

    @SerializedName("message-price")
    var messagePrice: String? = null

    @SerializedName("status")
    var status: String? = null

    @SerializedName("message-id")
    var messageId: String? = null

    @SerializedName("remaining-balance")
    var remainingBalance: String? = null
    var network: String? = null

    override fun toString(): String {
        return "Message{" +
                "to='" + to + '\'' +
                ", messagePrice='" + messagePrice + '\'' +
                ", status='" + status + '\'' +
                ", messageId='" + messageId + '\'' +
                ", remainingBalance='" + remainingBalance + '\'' +
                ", network='" + network + '\'' +
                '}'
    }
}

object Config {
    //Obtain these at https://www.nexmo.com/
    const val ApiKey = ""
    const val ApiSecret = ""
}
