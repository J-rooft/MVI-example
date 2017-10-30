package space.naboo.swplanets.view

import android.support.annotation.StringRes
import io.reactivex.Observable
import space.naboo.swplanets.model.PlanetsViewState

interface MainView {
    fun render(viewState: PlanetsViewState)

    fun loadMorePlanetsIntent(): Observable<Int>
    fun loadResidentIntent(): Observable<Int>

    fun getString(@StringRes resId: Int): String
}