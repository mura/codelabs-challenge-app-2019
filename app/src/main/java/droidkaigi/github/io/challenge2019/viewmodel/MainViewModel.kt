package droidkaigi.github.io.challenge2019.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import droidkaigi.github.io.challenge2019.data.HackerNewsRepository
import droidkaigi.github.io.challenge2019.data.api.response.Item
import droidkaigi.github.io.challenge2019.data.db.ArticlePreferences
import io.reactivex.Completable

class MainViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val STATE_STORIES = "stories"
    }

    fun articleIds(): Set<String> {
        return ArticlePreferences.getArticleIds(getApplication())
    }

    fun addArticleId(articleId: String) {
        ArticlePreferences.saveArticleIds(getApplication(), articleId)
    }

    val topStories = MutableLiveData<List<Item?>>()

    fun loadTopStories(): Completable {
        return HackerNewsRepository.topStories()
            .doOnSuccess { items ->
                topStories.postValue(items)
            }
            .ignoreElement()
    }

    fun loadSavedStories(bundle: Bundle?): Boolean {
        if (bundle == null) {
            return false
        }

        val savedStories = bundle.getSerializable(STATE_STORIES) as ArrayList<Item>? ?: return false

        topStories.value = savedStories
        return true
    }

    fun outState(): Bundle {
        val bundle = Bundle()
        bundle.putSerializable(STATE_STORIES, ArrayList<Item>(topStories.value.orEmpty()))
        return bundle
    }
}