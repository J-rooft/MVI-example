package space.naboo.swplanets.model

sealed class PlanetViewState {
    data class  LoadMorePlaceholder(val pageId: Int?) : PlanetViewState()
    object LoadInProgressPlaceholder : PlanetViewState()
    data class NormalPlanet(val planetName: String, val residentId: Int?) : PlanetViewState()
    data class PlanetWithLoadingResident(val planetName: String, val residentId: Int?) : PlanetViewState()
    data class PlanetWithResident(val planetName: String, val residentName: String) : PlanetViewState()

}
