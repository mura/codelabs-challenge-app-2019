package droidkaigi.github.io.challenge2019

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import droidkaigi.github.io.challenge2019.data.HackerNewsRepository
import droidkaigi.github.io.challenge2019.data.api.response.Item
import droidkaigi.github.io.challenge2019.viewmodel.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressView: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var storyAdapter: StoryAdapter

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        recyclerView = findViewById(R.id.item_recycler)
        progressView = findViewById(R.id.progress)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh)

        val itemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(itemDecoration)
        storyAdapter = StoryAdapter(
            stories = mutableListOf(),
            onClickItem = { item ->
                val intent = Intent(this@MainActivity, StoryActivity::class.java).apply {
                    putExtra(StoryActivity.EXTRA_ITEM_JSON, item)
                }
                startActivityForResult(intent)
            },
            onClickMenuItem = { item, menuItemId ->
                when (menuItemId) {
                    R.id.copy_url -> {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.primaryClip = ClipData.newPlainText("url", item.url)
                    }
                    R.id.refresh -> {
                        HackerNewsRepository.item(item.id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ newItem ->
                                val index = storyAdapter.stories.indexOf(item)
                                if (index == -1) return@subscribe

                                storyAdapter.stories[index] = newItem
                                runOnUiThread {
                                    storyAdapter.alreadyReadStories = viewModel.articleIds()
                                    storyAdapter.notifyItemChanged(index)
                                }
                            }, { throwable ->
                                showError(throwable)
                            })

                    }
                }
            },
            alreadyReadStories = viewModel.articleIds()
        )
        recyclerView.adapter = storyAdapter

        swipeRefreshLayout.setOnRefreshListener { loadTopStories() }


        val topStoriesObserver = Observer<List<Item?>> { items ->
            storyAdapter.stories = items?.toMutableList() ?: mutableListOf()
            storyAdapter.alreadyReadStories = viewModel.articleIds()
            storyAdapter.notifyDataSetChanged()
        }
        viewModel.topStories.observe(this, topStoriesObserver)

        if (viewModel.loadSavedStories(savedInstanceState)) {
            return
        }
        progressView.visibility = Util.setVisibility(true)
        loadTopStories()
    }

    private fun loadTopStories() {
        viewModel.loadTopStories()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(AndroidLifecycleScopeProvider.from(this))
            .subscribe({
                progressView.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            }, { throwable ->
                showError(throwable)
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                data?.getLongExtra(StoryActivity.READ_ARTICLE_ID, 0L)?.let { id ->
                    if (id != 0L) {
                        viewModel.addArticleId(id.toString())
                        storyAdapter.alreadyReadStories = viewModel.articleIds()
                        storyAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.refresh -> {
                progressView.visibility = Util.setVisibility(true)
                loadTopStories()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.apply {
            putAll(viewModel.outState())
        }

        super.onSaveInstanceState(outState)
    }
}
