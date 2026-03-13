package fr.isen.daloiso.disneyapp

import Categorie
import Film
import Franchise

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Home

import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.daloiso.disneyapp.Screens.FilmDetailScreen
import fr.isen.daloiso.disneyapp.Screens.FilmStatus
import fr.isen.daloiso.disneyapp.Screens.FranchiseDetailScreen
import fr.isen.daloiso.disneyapp.Screens.LoginScreen
import fr.isen.daloiso.disneyapp.Screens.MarketScreen
import fr.isen.daloiso.disneyapp.Screens.ProfileFilmsScreen
import fr.isen.daloiso.disneyapp.Screens.ProfileScreen
import fr.isen.daloiso.disneyapp.Screens.SearchScreen
import fr.isen.daloiso.disneyapp.Screens.SignupScreen

import fr.isen.daloiso.disneyapp.ui.theme.DisneyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder


private val BgGradient    = Brush.linearGradient(listOf(Color(0xFF1A5C6E), Color(0xFF071220)))
private val CardBg        = Color(0xFF1E3A45)
private val TextPrimary   = Color.White
private val TextSecondary = Color(0xFFB0C8D0)
private val AccentTeal    = Color(0xFF1DADC0)
private val screensWithoutBottomBar = listOf("register", "login", "film_detail", "franchise_detail")

object FilmSelection {
    var selectedFilm: Film? = null
}

object FranchiseSelection {
    var selectedFranchise: Franchise? = null
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DisneyTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "register"

                val bottomItems = listOf(
                    BottomNavItem("Accueil",   "home",    Icons.Filled.Home,   Icons.Outlined.Home),
                    BottomNavItem("Marché",    "market",  Icons.Filled.Store,  Icons.Outlined.Store),
                    BottomNavItem("Recherche", "search",  Icons.Filled.Search, Icons.Outlined.Search),
                    BottomNavItem("Profil",    "profile", Icons.Filled.Person, Icons.Outlined.Person)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = BgGradient)
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = {
                            val hideBar = currentRoute in screensWithoutBottomBar
                                    || currentRoute?.startsWith("profile_films/") == true
                            if (!hideBar) {
                                BottomAppBar(items = bottomItems, navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("register")    { SignupScreen(navController = navController) }
                            composable("login")       { LoginScreen(navController = navController) }
                            composable("home")        { HomeScreen(navController = navController) }
                            composable("market")      { MarketScreen(navController = navController) }
                            composable("profile")     { ProfileScreen(navController = navController) }
                            composable("search")      { SearchScreen(navController = navController) }
                            composable("profile_films/{statusName}") { backStackEntry ->
                                val statusName = backStackEntry.arguments?.getString("statusName") ?: return@composable
                                val status = FilmStatus.values().find { it.name == statusName } ?: return@composable
                                ProfileFilmsScreen(navController = navController, status = status)
                            }
                            composable("film_detail") {
                                FilmSelection.selectedFilm?.let { film ->
                                    FilmDetailScreen(navController = navController, film = film)
                                }
                            }
                            composable("franchise_detail") {
                                FranchiseSelection.selectedFranchise?.let { franchise ->
                                    FranchiseDetailScreen(navController = navController, franchise = franchise)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun HomeScreen(navController: NavHostController) {
    val categories = remember { mutableStateListOf<Categorie>() }

    LaunchedEffect(Unit) {
        DataBaseHelper().getCategories { categories.addAll(it) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Image(
                painter = painterResource(id = R.drawable.disneymoins),
                contentDescription = "Disney Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(top = 24.dp, start = 24.dp, bottom = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(categories) { categorie ->
            CategoryCarouselSection(categorie = categorie, navController = navController)
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun CategoryCarouselSection(categorie: Categorie, navController: NavHostController) {
    Column {
        Text(
            text = categorie.categorie,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categorie.franchises) { franchise ->
                FranchiseCard(franchise = franchise, navController = navController)
            }
        }
    }
}

@Composable
fun FranchiseCard(franchise: Franchise, navController: NavHostController) {
    var posterUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(franchise.nom) {
        posterUrl = fetchCollectionPoster(franchise.nom)
    }

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                FranchiseSelection.selectedFranchise = franchise
                navController.navigate("franchise_detail")
            }
    ) {
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = franchise.nom,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CardBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Movie,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000)),
                        startY = 120f
                    )
                )
        )
        Text(
            text = franchise.nom,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}

suspend fun fetchCollectionPoster(query: String): String? {
    return withContext(Dispatchers.IO) {
        val apiKey = "37b0694785cebb5ccca028e53f38e0cb"
        val base = "https://image.tmdb.org/t/p/w500"
        val simplified = query
            .replace(Regex("cinematic universe", RegexOption.IGNORE_CASE), "")
            .replace(Regex("universe", RegexOption.IGNORE_CASE), "")
            .replace(Regex("collection", RegexOption.IGNORE_CASE), "")
            .trim()

        fun extractPosterPath(url: String): String? {
            return try {
                val results = JSONObject(URL(url).readText()).getJSONArray("results")
                if (results.length() > 0) {
                    val path = results.getJSONObject(0).optString("poster_path", "")
                    if (path.isNotEmpty()) "$base$path" else null
                } else null
            } catch (e: Exception) { null }
        }

        val encodedFull = URLEncoder.encode(query, "UTF-8")
        val encodedSimple = URLEncoder.encode(simplified, "UTF-8")

        extractPosterPath("https://api.themoviedb.org/3/search/collection?api_key=$apiKey&query=$encodedFull&language=fr-FR")
            ?: extractPosterPath("https://api.themoviedb.org/3/search/collection?api_key=$apiKey&query=$encodedSimple&language=fr-FR")
            ?: extractPosterPath("https://api.themoviedb.org/3/search/movie?api_key=$apiKey&query=$encodedSimple&language=fr-FR")
    }
}
class DataBaseHelper {
    fun getCategories(handler: (List<Categorie>) -> Unit) {
        val database = Firebase.database
        val myRef = database.getReference("categories")

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rawValue = snapshot.value
                val disneyGson = Gson()
                val jsonString = disneyGson.toJson(rawValue)
                val type = object : TypeToken<List<Categorie>>() {}.type
                val categories: List<Categorie> = disneyGson.fromJson(jsonString, type)
                handler(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("dataBase", error.toString())
                handler(emptyList())
            }
        })
    }
}