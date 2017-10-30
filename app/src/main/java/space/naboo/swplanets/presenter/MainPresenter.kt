package space.naboo.swplanets.presenter

import android.net.Uri
import android.util.Log
import data.SwService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import space.naboo.swplanets.R
import space.naboo.swplanets.fastLazy
import space.naboo.swplanets.model.PartialPlanetsViewState
import space.naboo.swplanets.model.PlanetViewState
import space.naboo.swplanets.model.PlanetsData
import space.naboo.swplanets.model.PlanetsViewState
import space.naboo.swplanets.view.MainView

class MainPresenter : Presenter<MainView> {

    private lateinit var view: MainView

    private var disposable: Disposable? = null

    private val swService by fastLazy {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://swapi.co/api/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        retrofit.create(SwService::class.java)
    }

    companion object {
        private val TAG: String = MainPresenter::class.java.simpleName
    }

    private fun bindIntents(view: MainView) {
        val loadMoreIntent: Observable<PartialPlanetsViewState> = view.loadMorePlanetsIntent()
                .flatMap { pageId ->
                    swService.getPlanets(pageId)
                            .map<PartialPlanetsViewState> {
                                parseMoreLoaded(it)
                            }
                            .startWith(PartialPlanetsViewState.LoadingMore)
                            .onErrorReturn { PartialPlanetsViewState.ErrorLoadingMore(it, pageId) }
                            .subscribeOn(Schedulers.io())
                }

        val loadResidentIntent: Observable<PartialPlanetsViewState> = view.loadResidentIntent()
                .flatMap { id ->
                    swService.getResident(id)
                            .map<PartialPlanetsViewState> {
                                PartialPlanetsViewState.ResidentLoaded(id, it)
                            }
                            .startWith(PartialPlanetsViewState.LoadingResident(id))
                            .onErrorReturn { PartialPlanetsViewState.ErrorLoadingResident(it, id) }
                            .subscribeOn(Schedulers.io())
                }

        val allIntents = Observable.merge(loadMoreIntent, loadResidentIntent)
        val initialState = PlanetsViewState.Content(listOf(PlanetViewState.LoadMorePlaceholder(1)))
        val stateObservable = allIntents.scan(initialState, this::viewStateReducer)

        disposable = stateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.render(it)
                }, {
                    Log.e(TAG, "Exception in stateObservable", it)
                })
    }

    private fun parseMoreLoaded(it: PlanetsData): PartialPlanetsViewState.MoreLoaded {
        return PartialPlanetsViewState.MoreLoaded(
                parsePageId(it.next),
                it.results
                        .map {
                            if (it.residents?.isNotEmpty() == true) {
                                PlanetViewState.NormalPlanet(it.name, getResidentId(it.residents))
                            } else {
                                PlanetViewState.PlanetWithResident(it.name, getView().getString(R.string.no_residents))
                            }

                        }
                        .toList())
    }

    private fun parsePageId(next: String?): Int? {
        if (next == null) {
            return null
        }

        return Uri.parse(next).getQueryParameter("page").toInt()
    }

    private fun getResidentId(residents: List<String>): Int? {
        if (residents.isEmpty()) {
            return null
        }

        return parseResidentId(residents.first())
    }

    private fun parseResidentId(resident: String): Int {
        val from = resident.indexOf("/people/") + "/people/".length
        val to = resident.lastIndexOf("/")
        return resident.substring(from, to).toInt()
    }

    private fun viewStateReducer(previousState: PlanetsViewState, partialState: PartialPlanetsViewState): PlanetsViewState {
        if (partialState is PartialPlanetsViewState.ErrorLoadingMore) {
            val planets = previousState.planets
                    .filter {
                        it !is PlanetViewState.LoadMorePlaceholder && it !is PlanetViewState.LoadInProgressPlaceholder
                    }
                    .toMutableList()
            planets.add(PlanetViewState.LoadMorePlaceholder(partialState.pageId))
            return PlanetsViewState.Content(planets)
        }

        if (partialState is PartialPlanetsViewState.ErrorLoadingResident) {
            val planets = mutableListOf<PlanetViewState>()
            previousState.planets.forEach {
                if (it is PlanetViewState.PlanetWithLoadingResident && it.residentId == partialState.residentId) {
                    planets.add(PlanetViewState.PlanetWithResident(it.planetName, view.getString(R.string.error_happened)))
                } else {
                    planets.add(it)
                }
            }

            return PlanetsViewState.Content(planets)
        }

        if (partialState is PartialPlanetsViewState.LoadingMore) {
            val planets = previousState.planets
                    .filter {
                        it !is PlanetViewState.LoadMorePlaceholder && it !is PlanetViewState.LoadInProgressPlaceholder
                    }
                    .toMutableList()
            planets.add(PlanetViewState.LoadInProgressPlaceholder)
            return PlanetsViewState.Content(planets)
        }

        if (partialState is PartialPlanetsViewState.MoreLoaded) {
            val planets = previousState.planets
                    .filter {
                        it !is PlanetViewState.LoadMorePlaceholder && it !is PlanetViewState.LoadInProgressPlaceholder
                    }
                    .toMutableList()
            planets.addAll(partialState.newPlanets)
            if (partialState.nextPageId != null) {
                planets.add(PlanetViewState.LoadMorePlaceholder(partialState.nextPageId))
            }
            return PlanetsViewState.Content(planets)
        }

        if (partialState is PartialPlanetsViewState.LoadingResident) {
            val planets = mutableListOf<PlanetViewState>()
            previousState.planets.forEach {
                if (it is PlanetViewState.NormalPlanet && it.residentId == partialState.residentId) {
                    planets.add(PlanetViewState.PlanetWithLoadingResident(it.planetName, it.residentId))
                } else {
                    planets.add(it)
                }
            }

            return PlanetsViewState.Content(planets)
        }

        if (partialState is PartialPlanetsViewState.ResidentLoaded) {
            val planets = mutableListOf<PlanetViewState>()
            previousState.planets.forEach {
                if (it is PlanetViewState.PlanetWithLoadingResident && it.residentId == partialState.residentId) {
                    planets.add(PlanetViewState.PlanetWithResident(it.planetName, partialState.resident.name))
                } else {
                    planets.add(it)
                }
            }

            return PlanetsViewState.Content(planets)
        }

        throw IllegalArgumentException("Unknown partial state: $partialState")
    }

    override fun attachView(view: MainView) {
        this.view = view
        bindIntents(view)
    }

    override fun detachView() {
        // if we need to retain instance we can do it here
        disposable?.dispose()
    }

    override fun getView(): MainView = view

}
