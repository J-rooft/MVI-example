package space.naboo.swplanets

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import space.naboo.swplanets.data.PlanetsAdapter
import space.naboo.swplanets.model.PlanetsViewState
import space.naboo.swplanets.presenter.MainPresenter
import space.naboo.swplanets.view.MainView

class MainActivity : AppCompatActivity(), MainView {

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
    }

    private val loadMoreSubject = PublishSubject.create<Int>()
    private val loadResidentSubject = PublishSubject.create<Int>()

    private val presenter by fastLazy { MainPresenter() }
    private val adapter by fastLazy { PlanetsAdapter(loadMoreSubject, loadResidentSubject) }
    private val rootView by fastLazy { findViewById<View>(R.id.root) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.app_name)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        presenter.attachView(this)
    }

    override fun onDestroy() {
        presenter.detachView()

        super.onDestroy()
    }

    override fun render(viewState: PlanetsViewState) {
        if (viewState is PlanetsViewState.Error) {
            Log.e(TAG, "Error in render", viewState.error)
            Snackbar.make(rootView, "Error occurred", Snackbar.LENGTH_SHORT).show()
        }

        adapter.clear()
        adapter.addAll(viewState.planets)
    }

    override fun loadMorePlanetsIntent(): Observable<Int> = loadMoreSubject

    override fun loadResidentIntent(): Observable<Int> = loadResidentSubject
}

fun <T> fastLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)
