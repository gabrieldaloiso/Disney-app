package fr.isen.daloiso.disneyapp.Screens

import Film
import Franchise
import SousSaga
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Movie
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import fr.isen.daloiso.disneyapp.FilmSelection

private val FdCardBg   = Color(0xFF1E3A45)
private val FdTextPrim = Color.White
private val FdTextSec  = Color(0xFFB0C8D0)
private val FdAccent   = Color(0xFF1DADC0)

@Composable
fun FranchiseDetailScreen(navController: NavHostController, franchise: Franchise) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Retour",
                    tint = FdTextPrim
                )
            }
            Text(
                text = franchise.nom,
                color = FdTextPrim,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val sagas = franchise.sousSagas
        if (sagas != null) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(sagas) { saga ->
                    SagaCarouselSection(saga = saga, navController = navController)
                }
            }
        } else {
            val films = franchise.films ?: emptyList()
            Text(
                text = "${films.size} film${if (films.size > 1) "s" else ""}",
                color = FdTextSec,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(films) { film ->
                    FilmPosterCard(film = film, navController = navController)
                }
            }
        }
    }
}

@Composable
fun SagaCarouselSection(saga: SousSaga, navController: NavHostController) {
    Column {
        Text(
            text = saga.nom,
            color = FdTextPrim,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(saga.films) { film ->
                FilmPosterCard(film = film, navController = navController)
            }
        }
    }
}

@Composable
fun FilmPosterCard(film: Film, navController: NavHostController) {
    var posterUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(film.titre) {
        posterUrl = fetchPosterUrl(film.titre, film.annee)
    }

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                FilmSelection.selectedFilm = film
                navController.navigate("film_detail")
            }
    ) {
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = film.titre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FdCardBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Movie,
                    contentDescription = null,
                    tint = FdAccent,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xDD000000)),
                        startY = 90f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
        ) {
            Text(
                text = film.titre,
                color = FdTextPrim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )
            film.annee?.let {
                Text(
                    text = "$it",
                    color = FdTextSec,
                    fontSize = 10.sp
                )
            }
        }
    }
}
