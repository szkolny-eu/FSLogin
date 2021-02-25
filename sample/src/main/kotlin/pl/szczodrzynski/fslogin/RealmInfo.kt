package pl.szczodrzynski.fslogin

import pl.szczodrzynski.fslogin.realm.RealmData

data class RealmInfo(
    val id: Int,
    val name: String,
    val description: String,
    val icon: String,
    val screenshot: String,
    val formFields: List<String>,
    val realmData: RealmData
)
