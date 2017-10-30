package space.naboo.swplanets.data

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.subjects.PublishSubject
import space.naboo.swplanets.R
import space.naboo.swplanets.fastLazy
import space.naboo.swplanets.model.PlanetViewState

class PlanetsAdapter(private val loadMoreSubject: PublishSubject<Int>,
        private val loadResidentSubject: PublishSubject<Int>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val PLANET_VIEW_ITEM = 1
        const val LOAD_MORE_VIEW_ITEM = 2
        const val MORE_LOADING_VIEW_ITEM = 3
    }

    private val planets = ArrayList<PlanetViewState>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PLANET_VIEW_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.planet_item, parent, false)
                PlanetsViewHolder(view)
            }
            LOAD_MORE_VIEW_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.load_more_item, parent, false)
                LoadMoreViewHolder(view)
            }
            MORE_LOADING_VIEW_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.more_loading_item, parent, false)
                MoreLoadingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            PLANET_VIEW_ITEM -> {
                holder as PlanetsViewHolder
                val planet = planets[position]

                setupPlanetViewHolder(holder, planet)
            }
            LOAD_MORE_VIEW_ITEM -> {
                holder as LoadMoreViewHolder
                val loadMore = planets[position] as PlanetViewState.LoadMorePlaceholder
                loadMore.pageId?.let { pageId ->
                    holder.loadMoreButton.setOnClickListener {
                        loadMoreSubject.onNext(pageId)
                    }
                }

            }
            MORE_LOADING_VIEW_ITEM -> {
                holder as MoreLoadingViewHolder
            }
        }
    }

    private fun setupPlanetViewHolder(holder: PlanetsViewHolder, planet: PlanetViewState) {
        when (planet) {
            is PlanetViewState.NormalPlanet -> {
                holder.planetNameView.text = planet.planetName
                holder.residentNameView.visibility = View.GONE
                holder.progressView.visibility = View.GONE

                planet.residentId?.let { residentId ->
                    holder.itemView.setOnClickListener {
                        loadResidentSubject.onNext(residentId)
                    }
                }
            }
            is PlanetViewState.PlanetWithLoadingResident -> {
                holder.planetNameView.text = planet.planetName
                holder.residentNameView.visibility = View.GONE
                holder.progressView.visibility = View.VISIBLE

                holder.itemView.setOnClickListener(null)
            }
            is PlanetViewState.PlanetWithResident -> {
                holder.planetNameView.text = planet.planetName
                holder.progressView.visibility = View.GONE
                holder.residentNameView.visibility = View.VISIBLE
                holder.residentNameView.text = planet.residentName

                holder.itemView.setOnClickListener(null)
            }
        }
    }

    override fun getItemCount(): Int = planets.size

    override fun getItemViewType(position: Int): Int = when (planets[position]) {
        is PlanetViewState.LoadMorePlaceholder -> LOAD_MORE_VIEW_ITEM
        PlanetViewState.LoadInProgressPlaceholder -> MORE_LOADING_VIEW_ITEM
        is PlanetViewState.NormalPlanet -> PLANET_VIEW_ITEM
        is PlanetViewState.PlanetWithLoadingResident -> PLANET_VIEW_ITEM
        is PlanetViewState.PlanetWithResident -> PLANET_VIEW_ITEM
    }

    fun clear() {
        this.planets.clear()
        notifyDataSetChanged()
    }

    fun addAll(planets: List<PlanetViewState>) {
        this.planets.addAll(planets)
        notifyItemRangeInserted(this.planets.size - planets.size - 1, planets.size)
    }

    class PlanetsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val planetNameView: TextView by fastLazy { itemView.findViewById<TextView>(R.id.planet_name) }
        val residentNameView: TextView by fastLazy { itemView.findViewById<TextView>(R.id.resident_name) }
        val progressView: ProgressBar by fastLazy { itemView.findViewById<ProgressBar>(R.id.progress) }
    }
    class LoadMoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val loadMoreButton: Button by fastLazy { itemView.findViewById<Button>(R.id.load_more_button) }
    }
    class MoreLoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
