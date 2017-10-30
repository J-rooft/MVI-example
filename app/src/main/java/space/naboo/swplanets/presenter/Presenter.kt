package space.naboo.swplanets.presenter

interface Presenter<T> {

    fun getView(): T
    fun attachView(view: T)
    fun detachView()
}
