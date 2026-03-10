package fr.isen.daloiso.disneyapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import Film
import androidx.compose.ui.text.withStyle
import coil.compose.AsyncImage
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import fr.isen.daloiso.disneyapp.FilmSelection

private val GradTop    = Color(0xFF1A5C6E)
private val GradBot    = Color(0xFF071220)
private val Accent     = Color(0xFF1DADC0)
private val White      = Color(0xFFFFFFFF)
private val FieldBg    = Color(0xFFF5F5F5)
private val TextDark   = Color(0xFF1C1B1F)
private val GrayLight  = Color(0xFFB0B8C8)
private val CardBg     = Color(0x22FFFFFF)
private val CardBorder = Color(0x33FFFFFF)

private const val SEARCH_TMDB_KEY = "37b0694785cebb5ccca028e53f38e0cb"
private const val SEARCH_TMDB_IMG = "https://image.tmdb.org/t/p/w185"

suspend fun fetchSearchPoster(titre: String): String? = withContext(Dispatchers.IO) {
    try {
        val q = URLEncoder.encode(titre, "UTF-8")
        val url = "https://api.themoviedb.org/3/search/movie?api_key=$SEARCH_TMDB_KEY&query=$q&language=fr-FR"
        val json = JSONObject(URL(url).readText())
        val results = json.getJSONArray("results")
        if (results.length() > 0) {
            val path = results.getJSONObject(0).optString("poster_path", "")
            if (path.isNotEmpty()) "$SEARCH_TMDB_IMG$path" else null
        } else null
    } catch (e: Exception) { null }
}


data class SearchResult(
    val filmId: String,
    val titre: String,
    val annee: String,
    val universe: String,
    val genre: String = "",
    val numero: Int = 0
)

@Composable
fun SearchScreen(navController: NavHostController?) {
    var query         by remember { mutableStateOf("") }
    var allFilms      by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val films = mutableListOf<SearchResult>()
                snapshot.children.forEach { categorie ->
                    val universeName = categorie.child("categorie").getValue(String::class.java) ?: ""
                    categorie.child("franchises").children.forEach { franchise ->
                        // Films directs dans la franchise
                        franchise.child("films").children.forEach { film ->
                            val titre  = film.child("titre").getValue(String::class.java) ?: return@forEach
                            val annee  = film.child("annee").getValue(Long::class.java)?.toString()
                                ?: film.child("annee").getValue(String::class.java) ?: ""
                            val genre  = film.child("genre").getValue(String::class.java) ?: ""
                            val numero = film.child("numero").getValue(Long::class.java)?.toInt() ?: 0
                            films.add(SearchResult(film.key ?: "", titre, annee, universeName, genre, numero))
                        }

                        franchise.child("sous_sagas").children.forEach { saga ->
                            saga.child("films").children.forEach { film ->
                                val titre  = film.child("titre").getValue(String::class.java) ?: return@forEach
                                val annee  = film.child("annee").getValue(Long::class.java)?.toString()
                                    ?: film.child("annee").getValue(String::class.java) ?: ""
                                val genre  = film.child("genre").getValue(String::class.java) ?: ""
                                val numero = film.child("numero").getValue(Long::class.java)?.toInt() ?: 0
                                films.add(SearchResult(film.key ?: "", titre, annee, universeName, genre, numero))
                            }
                        }
                    }
                }
                allFilms = films
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        })
    }

    val results = remember(query, allFilms) {
        if (query.isBlank()) emptyList()
        else allFilms.filter {
            it.titre.contains(query, ignoreCase = true) ||
                    it.universe.contains(query, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = listOf(GradTop, GradBot)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
            ) {
                Text(
                    text       = "Recherche",
                    color      = White,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "Trouvez n'importe quel film Disney",
                    color      = GrayLight,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FieldBg, RoundedCornerShape(14.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint     = if (query.isNotEmpty()) Accent else TextDark.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 14.dp).size(22.dp)
                    )
                    TextField(
                        value         = query,
                        onValueChange = { query = it },
                        placeholder   = {
                            Text(
                                "Ex: Lion King, Marvel, Star Wars...",
                                color    = TextDark.copy(alpha = 0.4f),
                                fontSize = 15.sp
                            )
                        },
                        singleLine    = true,
                        colors        = TextFieldDefaults.colors(
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor             = Accent,
                            focusedTextColor        = TextDark,
                            unfocusedTextColor      = TextDark
                        ),
                        textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
                        modifier      = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Effacer",
                                tint     = TextDark.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Accent, strokeWidth = 2.5.dp, modifier = Modifier.size(36.dp))
                        }
                    }

                    query.isBlank() -> {
                        Column(
                            modifier            = Modifier.fillMaxSize().padding(top = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                tint     = CardBorder,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Commencez à taper pour chercher",
                                color    = GrayLight,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${allFilms.size} films disponibles",
                                color    = Accent.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                    }


                    results.isEmpty() -> {
                        Column(
                            modifier            = Modifier.fillMaxSize().padding(top = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint     = CardBorder,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Aucun film trouvé",
                                color      = White,
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "\"$query\" ne correspond à aucun film",
                                color    = GrayLight,
                                fontSize = 13.sp
                            )
                        }
                    }


                    else -> {
                        LazyColumn(
                            contentPadding        = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement   = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    "${results.size} résultat${if (results.size > 1) "s" else ""}",
                                    color    = GrayLight,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            items(results) { film ->
                                SearchResultCard(
                                    film      = film,
                                    query     = query,
                                    onClick   = {
                                        FilmSelection.selectedFilm = Film(
                                            titre  = film.titre,
                                            annee  = film.annee.toIntOrNull(),
                                            genre  = film.genre.ifBlank { null },
                                            numero = film.numero
                                        )
                                        navController?.navigate("film_detail")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(film: SearchResult, query: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        var posterUrl by remember(film.filmId) { mutableStateOf<String?>(null) }
        LaunchedEffect(film.filmId) { posterUrl = fetchSearchPoster(film.titre) }
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            if (posterUrl != null) {
                AsyncImage(
                    model              = posterUrl,
                    contentDescription = film.titre,
                    contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Outlined.Movie, null, tint = Accent, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            HighlightedText(
                text       = film.titre,
                highlight  = query,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Accent.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        film.universe,
                        color    = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (film.annee.isNotBlank()) {
                    Text(
                        "  •  ${film.annee}",
                        color    = GrayLight,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Icon(Icons.Outlined.ChevronRight, null, tint = GrayLight, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun HighlightedText(
    text      : String,
    highlight : String,
    fontSize  : androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight
) {
    if (highlight.isBlank()) {
        Text(text, color = White, fontSize = fontSize, fontWeight = fontWeight, maxLines = 1, overflow = TextOverflow.Ellipsis)
        return
    }

    val annotated = androidx.compose.ui.text.buildAnnotatedString {
        val lower      = text.lowercase()
        val lowerQuery = highlight.lowercase()
        var cursor     = 0
        while (cursor < text.length) {
            val idx = lower.indexOf(lowerQuery, cursor)
            if (idx == -1) {
                withStyle(androidx.compose.ui.text.SpanStyle(color = White)) { append(text.substring(cursor)) }
                break
            }
            if (idx > cursor) {
                withStyle(androidx.compose.ui.text.SpanStyle(color = White)) { append(text.substring(cursor, idx)) }
            }
            val end = idx + highlight.length
            withStyle(androidx.compose.ui.text.SpanStyle(color = Accent, fontWeight = FontWeight.ExtraBold)) {
                append(text.substring(idx, end))
            }
            cursor = end
        }
    }
    Text(annotated, fontSize = fontSize, maxLines = 1, overflow = TextOverflow.Ellipsis)
}