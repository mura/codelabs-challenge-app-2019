package droidkaigi.github.io.challenge2019.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import droidkaigi.github.io.challenge2019.data.db.ArticlePreferences

class MainViewModel(app: Application) : AndroidViewModel(app) {
    fun articleIds(): Set<String> {
        return ArticlePreferences.getArticleIds(getApplication())
    }

    fun addArticleId(articleId: String) {
        ArticlePreferences.saveArticleIds(getApplication(), articleId)
    }
}