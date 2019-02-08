package droidkaigi.github.io.challenge2019.data

import droidkaigi.github.io.challenge2019.data.api.HackerNewsApi
import droidkaigi.github.io.challenge2019.data.api.response.Item
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

object HackerNewsRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://hacker-news.firebaseio.com/v0/")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
    private val hackerNewsApi = retrofit.create(HackerNewsApi::class.java)

    fun topStories(): Single<List<Item>> {
        return hackerNewsApi.getTopStoriesAsSingle()
            .flatMapObservable { Observable.fromIterable(it.take(20)) }
            .flatMap { id -> hackerNewsApi.getItemAsSingle(id).toObservable() }
            .toList()
    }

    fun item(id: Long): Single<Item> {
        return hackerNewsApi.getItemAsSingle(id)
    }

    fun comments(ids: List<Long>): Single<List<Item>> {
        return Observable.fromIterable(ids)
            .flatMap { id -> hackerNewsApi.getItemAsSingle(id).toObservable() }
            .toList()
    }
}