package data

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import space.naboo.swplanets.model.PlanetsData
import space.naboo.swplanets.model.Resident

interface SwService {

    @GET("planets/?format=json")
    fun getPlanets(@Query("page") page: Int): Observable<PlanetsData>

    @GET("people/{id}/?format=json")
    fun getResident(@Path("id") id: Int): Observable<Resident>

}
