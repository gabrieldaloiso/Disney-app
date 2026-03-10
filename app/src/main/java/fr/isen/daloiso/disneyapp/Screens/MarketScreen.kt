package fr.isen.daloiso.disneyapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

private val MktGradTop    = Color(0xFF1A5C6E)
private val MktGradBot    = Color(0xFF071220)
private val MktAccent     = Color(0xFF1DADC0)
private val MktWhite      = Color(0xFFFFFFFF)
private val MktGray       = Color(0xFFB0B8C8)
private val MktCardBg     = Color(0x22FFFFFF)
private val MktCardBorder = Color(0x33FFFFFF)
private val MktGreen      = Color(0xFF27AE60)
private val MktOrange     = Color(0xFFE67E22)

data class MarketOffer(
    val filmId: String = "",
    val filmTitle: String = "",
    val sellerUid: String = "",
    val sellerEmail: String = ""
)

data class MarketNotification(
    val id: String = "",
    val message: String = ""
)

@Composable
fun MarketScreen(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uid   = currentUser?.uid ?: return

    var offers          by remember { mutableStateOf<List<MarketOffer>>(emptyList()) }
    var mySeekingIds    by remember { mutableStateOf<Set<String>>(emptySet()) }
    var myPossessedIds  by remember { mutableStateOf<Set<String>>(emptySet()) }
    var notifications   by remember { mutableStateOf<List<MarketNotification>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    DisposableEffect(uid) {
        val db = FirebaseDatabase.getInstance()

        val marketRef = db.getReference("market")
        val marketListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableListOf<MarketOffer>()
                snapshot.children.forEach { filmSnap ->
                    val filmId    = filmSnap.key ?: return@forEach
                    val filmTitle = filmSnap.child("title").getValue(String::class.java) ?: ""
                    filmSnap.child("sellers").children.forEach { sellerSnap ->
                        val sellerUid   = sellerSnap.key ?: return@forEach
                        if (sellerUid == uid) return@forEach
                        val sellerEmail = sellerSnap.child("email").getValue(String::class.java) ?: ""
                        result.add(MarketOffer(filmId, filmTitle, sellerUid, sellerEmail))
                    }
                }
                offers = result
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        marketRef.addValueEventListener(marketListener)

        val myFilmsRef = db.getReference("users/$uid/films")
        val myFilmsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mySeekingIds = snapshot.children.filter {
                    it.child("statuses").child(FilmStatus.OWNED.name).getValue(Boolean::class.java) == true
                }.mapNotNull { it.key }.toSet()
                myPossessedIds = snapshot.children.filter {
                    it.child("statuses").child(FilmStatus.POSSESSED.name).getValue(Boolean::class.java) == true
                }.mapNotNull { it.key }.toSet()
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        myFilmsRef.addValueEventListener(myFilmsListener)

        val notifRef = db.getReference("users/$uid/notifications")
        val notifListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notifications = snapshot.children.mapNotNull { snap ->
                    val id  = snap.key ?: return@mapNotNull null
                    val msg = snap.child("message").getValue(String::class.java) ?: return@mapNotNull null
                    MarketNotification(id, msg)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        notifRef.addValueEventListener(notifListener)

        onDispose {
            marketRef.removeEventListener(marketListener)
            myFilmsRef.removeEventListener(myFilmsListener)
            notifRef.removeEventListener(notifListener)
        }
    }

    fun claimOffer(offer: MarketOffer) {
        val db = FirebaseDatabase.getInstance()
        val buyerFilmRef = db.getReference("users/$uid/films/${offer.filmId}")

        buyerFilmRef.get().addOnSuccessListener { snap ->
            val existingStatuses = FilmStatus.values().filter {
                snap.child("statuses").child(it.name).getValue(Boolean::class.java) == true
            }.toMutableSet()
            existingStatuses.removeAll(incompatibleWith(FilmStatus.POSSESSED))
            existingStatuses.add(FilmStatus.POSSESSED)
            val statusesMap = FilmStatus.values().associate { it.name to (it in existingStatuses) }
            buyerFilmRef.child("title").setValue(offer.filmTitle)
            buyerFilmRef.child("statuses").setValue(statusesMap)
            scope.launch { snackbarHostState.showSnackbar("Vous avez obtenu le Blu-Ray \"${offer.filmTitle}\" !") }
        }

        val sellerFilmRef = db.getReference("users/${offer.sellerUid}/films/${offer.filmId}")
        sellerFilmRef.get().addOnSuccessListener { snap ->
            val sellerStatuses = FilmStatus.values().filter {
                snap.child("statuses").child(it.name).getValue(Boolean::class.java) == true
            }.toMutableSet()
            sellerStatuses.remove(FilmStatus.WANT_TO_SELL)
            if (sellerStatuses.isEmpty()) sellerFilmRef.removeValue()
            else sellerFilmRef.child("statuses").setValue(FilmStatus.values().associate { it.name to (it in sellerStatuses) })
        }

        db.getReference("users/${offer.sellerUid}/notifications/${offer.filmId}")
            .setValue(mapOf("message" to "Quelqu'un a récupéré votre Blu-Ray \"${offer.filmTitle}\""))
        db.getReference("market/${offer.filmId}/sellers/${offer.sellerUid}").removeValue()
        db.getReference("market/${offer.filmId}/seekers/$uid").removeValue()
    }

    fun dismissNotification(notif: MarketNotification) {
        FirebaseDatabase.getInstance().getReference("users/$uid/notifications/${notif.id}").removeValue()
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(MktGradTop, MktGradBot)))
                .padding(padding)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = "Marché",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MktWhite,
                        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 16.dp)
                    )
                }

                if (notifications.isNotEmpty()) {
                    items(notifications) { notif ->
                        NotificationBanner(
                            message   = notif.message,
                            onDismiss = { dismissNotification(notif) },
                            modifier  = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                if (offers.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Store, null, tint = MktCardBorder, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(10.dp))
                                Text("Aucune offre disponible", color = MktGray, fontSize = 14.sp)
                            }
                        }
                    }
                } else {
                    val sorted = offers.sortedByDescending { it.filmId in mySeekingIds }
                    items(sorted) { offer ->
                        val isMatch     = offer.filmId in mySeekingIds
                        val isPossessed = offer.filmId in myPossessedIds
                        OfferCard(
                            offer       = offer,
                            isMatch     = isMatch,
                            isPossessed = isPossessed,
                            onClaim     = { claimOffer(offer) },
                            modifier    = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun NotificationBanner(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MktGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(1.dp, MktGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.CheckCircle, null, tint = MktGreen, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, color = MktWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Outlined.Close, null, tint = MktGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun OfferCard(
    offer: MarketOffer,
    isMatch: Boolean,
    isPossessed: Boolean,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    var posterUrl by remember(offer.filmId) { mutableStateOf<String?>(null) }
    LaunchedEffect(offer.filmId) { posterUrl = fetchSearchPoster(offer.filmTitle) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isMatch) MktAccent.copy(alpha = 0.12f) else MktCardBg, RoundedCornerShape(14.dp))
            .border(1.dp, if (isMatch) MktAccent.copy(alpha = 0.6f) else MktCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MktOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (posterUrl != null) {
                AsyncImage(
                    model              = posterUrl,
                    contentDescription = offer.filmTitle,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Outlined.Movie, null, tint = MktOrange, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    offer.filmTitle,
                    color      = MktWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f, fill = false)
                )
                if (isMatch && !isPossessed) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier.background(MktAccent, RoundedCornerShape(50)).padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text("Match", color = MktWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        if (isPossessed) {
            Box(
                modifier = Modifier.background(MktGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Obtenu", color = MktGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick        = onClaim,
                colors         = ButtonDefaults.buttonColors(containerColor = MktAccent),
                shape          = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Obtenir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}