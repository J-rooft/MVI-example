package space.naboo.swplanets.model

data class PlanetsData(
        val count: Int,
        val next: String?,
        val previous: String?,
        val results: List<Planet>
)
