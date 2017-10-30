package space.naboo.swplanets.model

sealed class PlanetsViewState {
    data class Content(override val planets: List<PlanetViewState>) : PlanetsViewState()
    data class Error(val error: Throwable, override val planets: List<PlanetViewState>) : PlanetsViewState()

    abstract val planets: List<PlanetViewState>
}
