package space.naboo.swplanets.model

sealed class PartialPlanetsViewState {
    object LoadingMore : PartialPlanetsViewState()
    data class MoreLoaded(val nextPageId: Int?, val newPlanets: List<PlanetViewState>) : PartialPlanetsViewState()
    data class ErrorLoadingMore(val error: Throwable, val pageId: Int) : PartialPlanetsViewState()
    data class LoadingResident(val residentId: Int) : PartialPlanetsViewState()
    data class ResidentLoaded(val residentId: Int, val resident: Resident) : PartialPlanetsViewState()
    data class ErrorLoadingResident(val error: Throwable, val residentId: Int) : PartialPlanetsViewState()
}