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
import com.google.firebase.database.*

private val GradTop    = Color(0xFF1A5C6E)
private val GradBot    = Color(0xFF071220)
private val Accent     = Color(0xFF1DADC0)
private val White      = Color(0xFFFFFFFF)
private val FieldBg    = Color(0xFFF5F5F5)
private val TextDark   = Color(0xFF1C1B1F)
private val GrayLight  = Color(0xFFB0B8C8)
private val CardBg     = Color(0x22FFFFFF)
private val CardBorder = Color(0x33FFFFFF)

data class SearchResult(
    val filmId: String,
    val titre: String,
    val annee: String,
    val universe: String
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

                        franchise.child("films").children.forEach { film ->
                            val titre = film.child("titre").getValue(String::class.java) ?: return@forEach
                            val annee = film.child("annee").getValue(Long::class.java)?.toString()
                                ?: film.child("annee").getValue(String::class.java) ?: ""
                            films.add(SearchResult(film.key ?: "", titre, annee, universeName))
                        }

                        franchise.child("sous_sagas").children.forEach { saga ->
                            saga.child("films").children.forEach { film ->
                                val titre = film.child("titre").getValue(String::class.java) ?: return@forEach
                                val annee = film.child("annee").getValue(Long::class.java)?.toString()
                                    ?: film.child("annee").getValue(String::class.java) ?: ""
                                films.add(SearchResult(film.key ?: "", titre, annee, universeName))
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
                   // fontWeight = FontWeight.Bold
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
                                "Ex: Marvel, Star Wars...",
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
            .background(CardBg, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Movie,
                contentDescription = null,
                tint     = Accent,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {

            HighlightedText(
                text      = film.titre,
                highlight = query,
                fontSize  = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Accent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        film.universe,
                        color      = Accent,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                }
                if (film.annee.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Text(film.annee, color = GrayLight, fontSize = 12.sp)
                }
            }
        }

        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint     = GrayLight,
            modifier = Modifier.size(20.dp)
        )
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
    val annotated = buildAnnotatedText(text, highlight)
    Text(annotated, fontSize = fontSize, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

fun buildAnnotatedText(text: String, highlight: String): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    val lowerText = text.lowercase()
    val lowerHighlight = highlight.lowercase()
    var start = 0
    while (true) {
        val idx = lowerText.indexOf(lowerHighlight, start)
        if (idx == -1) {
            builder.append(
                androidx.compose.ui.text.AnnotatedString(
                    text,
                    listOf(androidx.compose.ui.text.AnnotatedString.Range(
                        androidx.compose.ui.text.SpanStyle(color = White),
                        start, text.length
                    ))
                )
            )
            break
        }

        if (idx > start) {
            builder.append(
                androidx.compose.ui.text.AnnotatedString(
                    text.substring(start, idx),
                    listOf(androidx.compose.ui.text.AnnotatedString.Range(
                        androidx.compose.ui.text.SpanStyle(color = White),
                        0, idx - start
                    ))
                )
            )
        }

        val end = idx + highlight.length
        builder.append(
            androidx.compose.ui.text.AnnotatedString(
                text.substring(idx, end),
                listOf(androidx.compose.ui.text.AnnotatedString.Range(
                    androidx.compose.ui.text.SpanStyle(
                        color      = Accent,
                        fontWeight = FontWeight.ExtraBold,
                        background = Accent.copy(alpha = 0.15f)
                    ),
                    0, end - idx
                ))
            )
        )
        start = end
    }
    return builder.toAnnotatedString()
}