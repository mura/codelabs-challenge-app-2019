package droidkaigi.github.io.challenge2019.data.api

import droidkaigi.github.io.challenge2019.data.api.response.Item
import droidkaigi.github.io.challenge2019.data.api.response.User
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface HackerNewsApi {
    @GET("item/{id}.json")
    fun getItem(@Path("id") id: Long): Call<Item>

    @GET("item/{id}.json")
    fun getItemAsSingle(@Path("id") id: Long): Single<Item>

    @GET("user/{id}.json")
    fun getUser(@Path("id") id: String): Call<User>

    @GET("topstories.json")
    fun getTopStories(): Call<List<Long>>

    @GET("topstories.json")
    fun getTopStoriesAsSingle(): Single<List<Long>>

    @GET("newstories.json")
    fun getNewStories(): Call<List<Long>>

    @GET("jobstories.json")
    fun getJobStories(): Call<List<Long>>
}
