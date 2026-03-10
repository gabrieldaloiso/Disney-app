package fr.isen.daloiso.disneyapp

import Categorie
import Film
import Franchise
import SousSaga
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.daloiso.disneyapp.Screens.LoginScreen
import fr.isen.daloiso.disneyapp.Screens.ProfileScreen
import fr.isen.daloiso.disneyapp.Screens.SignupScreen
import fr.isen.daloiso.disneyapp.ui.theme.DisneyTheme

// ── Couleurs ──────────────────────────────────────────────────────────────────
private val BgGradient    = Brush.linearGradient(listOf(Color(0xFF1A5C6E), Color(0xFF071220)))
private val Light         = Color(0xFFF5F5F5)
private val CardBg        = Color(0xFF1E3A45)
private val CardBgDeep    = Color(0xFF122533)
private val TextPrimary   = Color.White
private val TextSecondary = Color(0xFFB0C8D0)

private val screensWithoutBottomBar = listOf("register", "login")

// ── Activity ──────────────────────────────────────────────────────────────────
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
                    BottomNavItem("Accueil", "home",    Icons.Filled.Home,   Icons.Outlined.Home),
                    BottomNavItem("Profil",  "profile", Icons.Filled.Person, Icons.Outlined.Person)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = BgGradient)
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = {
                            if (currentRoute !in screensWithoutBottomBar) {
                                BottomAppBar(items = bottomItems, navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("register") { SignupScreen(navController = navController) }
                            composable("login")    { LoginScreen(navController = navController) }
                            composable("home")     { HomeScreen(navController = navController) }
                            composable("profile")  { ProfileScreen(navController = navController) }
                        }
                    }
                }
            }
        }
    }
}

// ── Home ──────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(navController: NavHostController) {
    val categories = remember { mutableStateListOf<Categorie>() }
    val expandableCategories = remember { mutableStateListOf<Categorie>() }

    LaunchedEffect(Unit) {
        DataBaseHelper().getCategories { categories.addAll(it) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.disneymoins),
            contentDescription = "Disney Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(top = 24.dp, start = 24.dp, bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            items(categories) { categorie ->
                val isExpanded = expandableCategories.any { it.categorie == categorie.categorie }
                CategoryCard(
                    categorie = categorie,
                    isExpanded = isExpanded,
                    onToggle = {
                        if (isExpanded) expandableCategories.removeAll { it.categorie == categorie.categorie }
                        else expandableCategories.add(categorie)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ── Catégorie ─────────────────────────────────────────────────────────────────
@Composable
fun CategoryCard(categorie: Categorie, isExpanded: Boolean, onToggle: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categorie.categorie,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Light
                )
            }
            if (isExpanded) {
                FranchiseList(franchises = categorie.franchises)
            }
        }
    }
}

// ── Franchises ────────────────────────────────────────────────────────────────
@Composable
fun FranchiseList(franchises: List<Franchise>) {
    val expandableFranchises = remember { mutableStateListOf<Franchise>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBgDeep)
            .padding(start = 16.dp, bottom = 8.dp)
    ) {
        franchises.forEach { franchise ->
            val isExpanded = expandableFranchises.any { it.nom == franchise.nom }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isExpanded) expandableFranchises.removeAll { it.nom == franchise.nom }
                        else expandableFranchises.add(franchise)
                    }
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "▸  ${franchise.nom}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Light,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Light
                )
            }
            if (isExpanded) {
                val sagas = franchise.sousSagas
                if (sagas != null) SagaList(sagas) else FilmList(franchise.tousLesFilms())
            }
        }
    }
}

// ── Sous-sagas ────────────────────────────────────────────────────────────────
@Composable
fun SagaList(sousSagas: List<SousSaga>) {
    val expandableSaga = remember { mutableStateListOf<SousSaga>() }

    Column(modifier = Modifier.padding(start = 16.dp)) {
        sousSagas.forEach { saga ->
            val isExpanded = expandableSaga.any { it.nom == saga.nom }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isExpanded) expandableSaga.removeAll { it.nom == saga.nom }
                        else expandableSaga.add(saga)
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◆  ${saga.nom}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            if (isExpanded) {
                FilmList(saga.films)
            }
        }
    }
}

// ── Films ─────────────────────────────────────────────────────────────────────
@Composable
fun FilmList(films: List<Film>) {
    Column(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)) {
        films.forEach { film ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "•", color = Light, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                Text(text = film.titre, fontSize = 13.sp, color = TextPrimary)
                film.annee?.let {
                    Text(text = "  ($it)", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

// ── Firebase helper ───────────────────────────────────────────────────────────
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
