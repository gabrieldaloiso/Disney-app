package fr.isen.daloiso.disneyapp.Screens

import Film
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
private val Accent        = Color(0xFF1DADC0)
private val CardBg        = Color(0xFF1E3A45)
private val CardBgDeep    = Color(0xFF122533)
private val TextPrimary   = Color.White
private val TextSecondary = Color(0xFFB0C8D0)
private val Light         = Color(0xFFF5F5F5)
private val Divider       = Color(0xFF1A5C6E)

private const val TMDB_API_KEY   = "37b0694785cebb5ccca028e53f38e0cb"
private const val TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w500"

data class TmdbMovieData(
    val posterUrl: String?,
    val voteAverage: Double?,
    val voteCount: Int?
)

suspend fun fetchTmdbData(titre: String, annee: Int?): TmdbMovieData {
    return withContext(Dispatchers.IO) {
        try {
            val query = URLEncoder.encode(titre, "UTF-8")
            val yearParam = if (annee != null) "&year=$annee" else ""
            val url = "https://api.themoviedb.org/3/search/movie?api_key=$TMDB_API_KEY&query=$query$yearParam&language=fr-FR"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")
            if (results.length() > 0) {
                val movie = results.getJSONObject(0)
                val posterPath = movie.optString("poster_path", "")
                val posterUrl = if (posterPath.isNotEmpty()) "$TMDB_IMAGE_URL$posterPath" else null
                val voteAverage = movie.optDouble("vote_average").takeIf { !it.isNaN() }
                val voteCount = movie.optInt("vote_count").takeIf { it > 0 }
                TmdbMovieData(posterUrl, voteAverage, voteCount)
            } else TmdbMovieData(null, null, null)
        } catch (e: Exception) {
            TmdbMovieData(null, null, null)
        }
    }
}

// Kept for backward compatibility with other screens
suspend fun fetchPosterUrl(titre: String, annee: Int?): String? =
    fetchTmdbData(titre, annee).posterUrl

fun incompatibleWith(status: FilmStatus): Set<FilmStatus> = when (status) {
    FilmStatus.WATCHED       -> setOf(FilmStatus.WANT_TO_WATCH)
    FilmStatus.WANT_TO_WATCH -> setOf(FilmStatus.WATCHED)
    FilmStatus.OWNED         -> setOf(FilmStatus.WANT_TO_SELL, FilmStatus.POSSESSED)
    FilmStatus.WANT_TO_SELL  -> setOf(FilmStatus.OWNED, FilmStatus.POSSESSED)
    FilmStatus.POSSESSED     -> setOf(FilmStatus.OWNED, FilmStatus.WANT_TO_SELL)
}

@Composable
fun FilmDetailScreen(navController: NavHostController, film: Film) {
    var posterUrl    by remember { mutableStateOf<String?>(null) }
    var voteAverage  by remember { mutableStateOf<Double?>(null) }
    var voteCount    by remember { mutableStateOf<Int?>(null) }

    val uid    = FirebaseAuth.getInstance().currentUser?.uid
    val filmId = film.titre.replace(Regex("[^A-Za-z0-9]"), "_")
    var currentStatuses by remember { mutableStateOf<Set<FilmStatus>>(emptySet()) }

    LaunchedEffect(film.titre) {
        val data = fetchTmdbData(film.titre, film.annee)
        posterUrl   = data.posterUrl
        voteAverage = data.voteAverage
        voteCount   = data.voteCount
    }

    LaunchedEffect(filmId, uid) {
        uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance()
            .getReference("users/$uid/films/$filmId")
            .get()
            .addOnSuccessListener { snap ->
                val statusesNode = snap.child("statuses")
                currentStatuses = if (statusesNode.exists()) {
                    FilmStatus.values().filter {
                        statusesNode.child(it.name).getValue(Boolean::class.java) == true
                    }.toSet()
                } else {
                    val s = FilmStatus.values().find { it.name == snap.child("status").getValue(String::class.java) }
                    if (s != null) setOf(s) else emptySet()
                }
            }
    }

    fun toggleStatus(status: FilmStatus) {
        uid ?: return
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        val db        = FirebaseDatabase.getInstance()
        val filmRef   = db.getReference("users/$uid/films/$filmId")
        val marketRef = db.getReference("market/$filmId")

        val newStatuses = if (status in currentStatuses) {
            currentStatuses - status
        } else {
            (currentStatuses - incompatibleWith(status)) + status
        }
        currentStatuses = newStatuses

        if (newStatuses.isEmpty()) {
            filmRef.removeValue()
        } else {
            filmRef.child("title").setValue(film.titre)
            val statusesMap = FilmStatus.values().associate { it.name to (it in newStatuses) }
            filmRef.child("statuses").setValue(statusesMap)
        }

        marketRef.child("sellers/$uid").removeValue()
        marketRef.child("seekers/$uid").removeValue()
        if (newStatuses.isNotEmpty()) {
            marketRef.child("title").setValue(film.titre)
            if (FilmStatus.WANT_TO_SELL in newStatuses)
                marketRef.child("sellers/$uid").setValue(mapOf("email" to userEmail))
            if (FilmStatus.OWNED in newStatuses)
                marketRef.child("seekers/$uid").setValue(mapOf("email" to userEmail))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Retour",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Détail du film",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = film.titre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(250.dp)
                    .height(350.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Accent.copy(alpha = 0.4f),
                        spotColor = Accent.copy(alpha = 0.6f)
                    )
                    .clip(RoundedCornerShape(24.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(300.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardBgDeep),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Movie,
                    contentDescription = null,
                    tint = Accent.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = film.titre,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        film.genre?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = it,
                fontSize = 22.sp,
                fontStyle = FontStyle.Italic,
                color = Accent,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                if (film.numero != 0) {
                    InfoRow(icon = Icons.Outlined.Tag, label = "Numéro", value = "#${film.numero}")
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 12.dp))
                }

                film.annee?.let {
                    InfoRow(icon = Icons.Outlined.CalendarMonth, label = "Année de sortie", value = it.toString())
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 12.dp))
                }

                film.genre?.let {
                    InfoRow(icon = Icons.Outlined.Movie, label = "Genre", value = it)
                }

                voteAverage?.let { score ->
                    HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "  Score TMDB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val scoreColor = when {
                                score >= 7.0 -> Color(0xFF1DB085)
                                score >= 5.0 -> Color(0xFFF0A500)
                                else         -> Color(0xFFE05252)
                            }
                            Text(
                                text = "%.1f/10".format(score),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor
                            )
                            voteCount?.let { count ->
                                Text(
                                    text = "($count votes)",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                if (film.annee == null && film.genre == null && voteAverage == null) {
                    Text(
                        text = "Aucune information disponible.",
                        fontSize = 20.sp,
                        fontStyle = FontStyle.Italic,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            FilmStatus.values().forEach { status ->
                StatusCircleButton(status, status in currentStatuses) { toggleStatus(status) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "  $label",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Light
        )
    }
}
@Composable
fun StatusCircleButton(status: FilmStatus, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(56.dp)
            .background(
                color = if (isSelected) status.color else status.color.copy(alpha = 0.15f),
                shape = CircleShape
            )
            .border(2.dp, status.color, CircleShape)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = status.icon,
            contentDescription = status.label,
            tint = if (isSelected) Color.White else status.color,
            modifier = Modifier.size(26.dp)
        )
    }
}
