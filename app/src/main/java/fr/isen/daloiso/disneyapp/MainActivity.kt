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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import fr.isen.daloiso.disneyapp.Screens.LoginScreen
import fr.isen.daloiso.disneyapp.Screens.SignupScreen
import fr.isen.daloiso.disneyapp.ui.theme.DisneyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DisneyTheme {
                val navController = rememberNavController()
                val startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "register"
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("register") { SignupScreen(navController = navController) }
                    composable("login") { LoginScreen(navController = navController) }
                    composable("home") {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            val categories = remember { mutableStateListOf<Categorie>() }
                            val expandableCategories = remember { mutableStateListOf<Categorie>() }
                            LaunchedEffect(Unit) {
                                DataBaseHelper().getCategories { categories.addAll(it) }
                            }
                            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                                items(categories) { categorie ->
                                    Column {
                                        Card(Modifier.clickable {
                                            if (expandableCategories.firstOrNull { it.categorie == categorie.categorie } != null) {
                                                expandableCategories.removeAll { it.categorie == categorie.categorie }
                                            } else {
                                                expandableCategories.add(categorie)
                                            }
                                        }) {
                                            Text("${categorie.categorie}")
                                        }
                                        if (expandableCategories.firstOrNull { it.categorie == categorie.categorie } != null) {
                                            franchises(categorie.franchises)
                                        }
                                    }
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
fun franchises(franchises: List<Franchise>) {
    val expandableFranchises = remember {
        mutableStateListOf<Franchise>()
    }

    Column(Modifier.padding(start = 16.dp)) {
        franchises.forEach { franchise ->
            Text(franchise.nom, Modifier.clickable {
                if (expandableFranchises.firstOrNull { it.nom == franchise.nom } != null) {
                    expandableFranchises.removeAll { it.nom == franchise.nom }
                } else {
                    expandableFranchises.add(franchise)
                }
            })
            if(expandableFranchises.firstOrNull { it.nom == franchise.nom } != null) {
                val saga = franchise.sousSagas
                saga?.let { saga(it) } ?: run { films(franchise.tousLesFilms()) }
            }
        }
    }
}

@Composable
fun saga(sousSaga: List<SousSaga>) {
    val expandableSaga = remember {
        mutableStateListOf<SousSaga>()
    }

    Column(Modifier.padding(start = 16.dp)) {
        sousSaga.forEach { saga ->
            Text(saga.nom, Modifier.clickable {
                if (expandableSaga.firstOrNull { it.nom == saga.nom } != null) {
                    expandableSaga.removeAll { it.nom == saga.nom }
                } else {
                    expandableSaga.add(saga)
                }
            })
            if(expandableSaga.firstOrNull { it.nom == saga.nom } != null) {
                films(saga.films)
            }
        }
    }
}

@Composable
fun films(films: List<Film>) {
    Column(Modifier.padding(start = 16.dp)) {
        films.forEach { film ->
            Text(film.titre)
        }
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
