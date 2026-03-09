package fr.isen.daloiso.disneyapp.Screens

import fr.isen.daloiso.disneyapp.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
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

data class AvatarOption(val id: String, val label: String, val resId: Int, val fallback: String)

val AVATAR_LIST = listOf(
    AvatarOption("minnie",  "Minnie",  R.drawable.minnie,  "Mi"),
    AvatarOption("simba",   "Simba",   R.drawable.simba,   "S"),
    AvatarOption("nemo",    "Nemo",    R.drawable.nemo,    "N"),
    AvatarOption("stitch",  "Stitch",  R.drawable.stitech, "St"),
    AvatarOption("moana",   "Moana",   R.drawable.moana,   "Mo"),
    AvatarOption("buzz",    "Buzz",    R.drawable.buzz,    "B"),
    AvatarOption("elsa",    "Elsa",    R.drawable.elsa,    "E"),
)

data class UserFilmEntry(
    val filmId: String = "",
    val title: String = "",
    val status: FilmStatus = FilmStatus.WATCHED
)

enum class FilmStatus(val label: String, val icon: ImageVector, val color: Color) {
    WATCHED(      "Vu",      Icons.Outlined.Visibility, Accent),
    WANT_TO_WATCH("À voir",  Icons.Outlined.Bookmark,   Color(0xFF9B59B6)),
    OWNED(        "Possédé", Icons.Outlined.Album,      Color(0xFF27AE60)),
    WANT_TO_SELL( "À céder", Icons.Outlined.Sell,       Color(0xFFE67E22))
}

@Composable
fun ProfileScreen(navController: NavHostController?) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var displayName      by remember { mutableStateOf(user?.displayName ?: "Utilisateur") }
    var userEmail        by remember { mutableStateOf(user?.email ?: "") }
    var filmEntries      by remember { mutableStateOf<List<UserFilmEntry>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(true) }
    var activeFilter     by remember { mutableStateOf<FilmStatus?>(null) }
    var showLogout       by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var selectedAvatarId by remember { mutableStateOf("mickey") }
    val uid = user?.uid

    // Firebase
    DisposableEffect(uid) {
        if (uid == null) { isLoading = false; return@DisposableEffect onDispose {} }
        val db = FirebaseDatabase.getInstance()
        db.getReference("users/$uid/avatarId").get().addOnSuccessListener { snap ->
            snap.getValue(String::class.java)?.let { selectedAvatarId = it }
        }
        val filmsRef = db.getReference("users/$uid/films")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                filmEntries = snapshot.children.mapNotNull { child ->
                    val filmId    = child.key ?: return@mapNotNull null
                    val title     = child.child("title").getValue(String::class.java) ?: ""
                    val statusStr = child.child("status").getValue(String::class.java) ?: ""
                    val status    = FilmStatus.values().find { it.name == statusStr } ?: FilmStatus.WATCHED
                    UserFilmEntry(filmId, title, status)
                }
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) { isLoading = false }
        }
        filmsRef.addValueEventListener(listener)
        onDispose { filmsRef.removeEventListener(listener) }
    }

    fun saveAvatar(id: String) {
        uid ?: return
        FirebaseDatabase.getInstance().getReference("users/$uid/avatarId").setValue(id)
        selectedAvatarId = id
    }
    fun removeFilm(entry: UserFilmEntry) {
        uid ?: return
        FirebaseDatabase.getInstance().getReference("users/$uid/films/${entry.filmId}").removeValue()
    }

    val avatar   = AVATAR_LIST.find { it.id == selectedAvatarId } ?: AVATAR_LIST[0]
    val filtered = if (activeFilter == null) filmEntries else filmEntries.filter { it.status == activeFilter }

    // Dialogs
    if (showAvatarPicker) {
        AvatarPickerDialog(
            current   = selectedAvatarId,
            onSelect  = { saveAvatar(it); showAvatarPicker = false },
            onDismiss = { showAvatarPicker = false }
        )
    }
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            containerColor   = Color(0xFF0E2030),
            shape            = RoundedCornerShape(16.dp),
            title  = { Text("Déconnexion", color = White, fontWeight = FontWeight.Bold) },
            text   = { Text("Voulez-vous vraiment vous déconnecter ?", color = GrayLight) },
            confirmButton = {
                Button(
                    onClick = { auth.signOut(); navController?.navigate("login") { popUpTo(0) { inclusive = true } } },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape   = RoundedCornerShape(8.dp)
                ) { Text("Déconnecter", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text("Annuler", color = GrayLight) }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = listOf(GradTop, GradBot)))
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {

            item {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(top = 56.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar cliquable
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showAvatarPicker = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(Accent.copy(alpha = 0.18f), CircleShape)
                                .border(2.5.dp, Accent, CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Image chargée avec coil si disponible, sinon fallback initiales
                            AvatarImage(avatar = avatar)
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(Accent, CircleShape)
                                .border(2.dp, GradBot, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Edit, null, tint = White, modifier = Modifier.size(13.dp))
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(avatar.label, color = Accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text       = displayName,
                        color      = White,
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))
                    Text(userEmail, color = GrayLight, fontSize = 15.sp)

                    Spacer(Modifier.height(24.dp))

                    // Stats
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilmStatus.values().forEach { status ->
                            val count = filmEntries.count { it.status == status }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .background(CardBg, RoundedCornerShape(10.dp))
                                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(count.toString(), color = White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(status.label, color = GrayLight, fontSize = 13.sp)
                            }
                        }
                    }


                }
            }


            item {
                Divider(color = CardBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ma collection", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${filmEntries.size} film${if (filmEntries.size > 1) "s" else ""}", color = GrayLight, fontSize = 15.sp)
                }
                Spacer(Modifier.height(14.dp))
            }

            item {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.padding(bottom = 16.dp)
                ) {
                    item {
                        FilterPill("Tous", activeFilter == null, Accent) { activeFilter = null }
                    }
                    items(FilmStatus.values()) { s ->
                        FilterPill(s.label, activeFilter == s, s.color, s.icon) {
                            activeFilter = if (activeFilter == s) null else s
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent, strokeWidth = 2.5.dp, modifier = Modifier.size(32.dp))
                    }
                }
            } else if (filtered.isEmpty()) {
                item {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Movie, null, tint = CardBorder, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("Aucun film dans cette liste", color = GrayLight, fontSize = 14.sp)
                    }
                }
            } else {
                items(filtered) { entry ->
                    FilmCard(entry = entry, onRemove = { removeFilm(entry) })
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                TextButton(
                    onClick = { showLogout = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .background(Color(0x22E53935), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Outlined.Logout, null, tint = Color(0xFFE57373), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Se déconnecter", color = Color(0xFFE57373), fontWeight = FontWeight.Medium, fontSize = 17.sp)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AvatarImage(avatar: AvatarOption) {
    androidx.compose.foundation.Image(
        painter            = androidx.compose.ui.res.painterResource(id = avatar.resId),
        contentDescription = avatar.label,
        contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
        modifier           = Modifier.fillMaxSize().clip(CircleShape)
    )
}

@Composable
fun AvatarPickerDialog(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = listOf(GradTop, GradBot)), RoundedCornerShape(20.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Changer d'avatar", color = White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Close, null, tint = GrayLight, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Sélectionnez un personnage", color = GrayLight, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))

                AVATAR_LIST.chunked(4).forEach { row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { av ->
                            val selected = av.id == current
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Accent.copy(alpha = 0.2f) else CardBg)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) Accent else CardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelect(av.id) }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Accent.copy(alpha = 0.15f), CircleShape)
                                        .border(1.5.dp, if (selected) Accent else CardBorder, CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AvatarImage(avatar = av)
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    text       = av.label,
                                    color      = if (selected) Accent else GrayLight,
                                    fontSize   = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign  = TextAlign.Center,
                                    maxLines   = 1
                                )
                            }
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun FilterPill(label: String, selected: Boolean, color: Color, icon: ImageVector? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) color else CardBg)
            .border(1.dp, if (selected) Color.Transparent else color.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (selected) White else color, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(label, color = if (selected) White else color, fontSize = 15.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun FilmCard(entry: UserFilmEntry, onRemove: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor   = Color(0xFF0E2030),
            shape            = RoundedCornerShape(16.dp),
            title  = { Text("Retirer de la collection ?", color = White, fontWeight = FontWeight.Bold) },
            text   = { Text("\"${entry.title}\" sera retiré.", color = GrayLight, fontSize = 14.sp) },
            confirmButton = {
                Button(onClick = { onRemove(); showConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), shape = RoundedCornerShape(8.dp)) {
                    Text("Retirer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Annuler", color = GrayLight) } }
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .background(CardBg, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(46.dp).background(entry.status.color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(entry.status.icon, null, tint = entry.status.color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.title, color = White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(entry.status.label, color = entry.status.color, fontSize = 13.sp)
        }
        IconButton(onClick = { showConfirm = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.Close, null, tint = GrayLight, modifier = Modifier.size(16.dp))
        }
    }
}