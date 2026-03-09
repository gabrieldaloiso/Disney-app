import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────
// Racine du JSON Firebase
// ─────────────────────────────────────────────

data class DisneyDatabase(
    @SerializedName("categories")
    val categories: List<Categorie> = emptyList()
)

// ─────────────────────────────────────────────
// Catégorie (ex: "Grandes Sagas", "Touchstone")
// ─────────────────────────────────────────────

data class Categorie(
    @SerializedName("categorie")
    val categorie: String = "",

    @SerializedName("franchises")
    val franchises: List<Franchise> = emptyList()
)

// ─────────────────────────────────────────────
// Franchise (ex: "Star Wars", "Marvel Cinematic Universe")
// Peut contenir soit des sous_sagas, soit des films directement
// ─────────────────────────────────────────────

data class Franchise(
    @SerializedName("nom")
    val nom: String = "",

    // Franchises avec sous-sagas (ex: Star Wars, Spider-Man, Alien et Predator)
    @SerializedName("sous_sagas")
    val sousSagas: List<SousSaga>? = null,

    // Franchises sans sous-sagas (liste de films directe)
    @SerializedName("films")
    val films: List<Film>? = null
) {
    /**
     * Retourne tous les films de la franchise,
     * qu'ils soient dans des sous-sagas ou directement rattachés.
     */
    fun tousLesFilms(): List<Film> {
        return sousSagas?.flatMap { it.films } ?: films ?: emptyList()
    }
}

// ─────────────────────────────────────────────
// Sous-saga (ex: "Saga Skywalker", "Phase 1", "Reboot")
// ─────────────────────────────────────────────

data class SousSaga(
    @SerializedName("nom")
    val nom: String = "",

    @SerializedName("films")
    val films: List<Film> = emptyList()
)

// ─────────────────────────────────────────────
// Film
// ─────────────────────────────────────────────

data class Film(
    @SerializedName("numero")
    val numero: Int = 0,

    @SerializedName("titre")
    val titre: String = "",

    @SerializedName("genre")
    val genre: String? = null,

    @SerializedName("annee")
    val annee: Int? = null
)