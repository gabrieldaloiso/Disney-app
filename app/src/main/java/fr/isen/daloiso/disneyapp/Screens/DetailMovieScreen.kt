package fr.isen.daloiso.disneyapp.Screens

import Film
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

// ── Couleurs ──────────────────────────────────────────────────────────────────
private val AccentPurple  = Color(0xFF6650A4)
private val CardBg        = Color(0xFF1E3A45)
private val TextPrimary   = Color.White
private val TextSecondary = Color(0xFFB0C8D0)
private val Light         = Color(0xFFF5F5F5)
private val Divider       = Color(0xFF1A5C6E)

// ── Écran détail film ─────────────────────────────────────────────────────────
@Composable
fun FilmDetailScreen(navController: NavHostController, film: Film) {
    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ────────────────────────────────────────────────────────────
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
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Icône + Titre ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Movie,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(56.dp)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = film.titre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                film.genre?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = AccentPurple
                    )
                }
            }
        }

        // ── Carte infos ───────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBg),
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

                if (film.annee == null && film.genre == null) {
                    Text(
                        text = "Aucune information disponible.",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ── Ligne d'info ──────────────────────────────────────────────────────────────
@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentPurple,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "  $label",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Light
        )
    }
}