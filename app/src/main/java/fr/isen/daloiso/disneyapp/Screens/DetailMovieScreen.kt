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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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
    val uid    = FirebaseAuth.getInstance().currentUser?.uid
    val filmId = film.titre.replace(Regex("[^A-Za-z0-9]"), "_")

    var currentStatus by remember { mutableStateOf<FilmStatus?>(null) }

    LaunchedEffect(filmId, uid) {
        uid ?: return@LaunchedEffect
        FirebaseDatabase.getInstance()
            .getReference("users/$uid/films/$filmId/status")
            .get()
            .addOnSuccessListener { snap ->
                val statusStr = snap.getValue(String::class.java)
                currentStatus = FilmStatus.values().find { it.name == statusStr }
            }
    }

    fun saveStatus(status: FilmStatus) {
        uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid/films/$filmId")
        if (currentStatus == status) {
            ref.removeValue()
            currentStatus = null
        } else {
            ref.setValue(mapOf("title" to film.titre, "status" to status.name))
            currentStatus = status
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

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

        // ── Boutons collection en cercle ──────────────────────────────────────
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            val statuses = FilmStatus.values()
            StatusCircleButton(statuses[0], currentStatus == statuses[0], Modifier.align(Alignment.TopCenter))    { saveStatus(statuses[0]) }
            StatusCircleButton(statuses[1], currentStatus == statuses[1], Modifier.align(Alignment.CenterEnd))    { saveStatus(statuses[1]) }
            StatusCircleButton(statuses[2], currentStatus == statuses[2], Modifier.align(Alignment.BottomCenter)) { saveStatus(statuses[2]) }
            StatusCircleButton(statuses[3], currentStatus == statuses[3], Modifier.align(Alignment.CenterStart))  { saveStatus(statuses[3]) }
        }

        Spacer(modifier = Modifier.height(24.dp))
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

// ── Bouton circulaire statut ───────────────────────────────────────────────────
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
