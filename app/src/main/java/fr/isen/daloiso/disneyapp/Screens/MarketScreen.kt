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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

// ── Couleurs ──────────────────────────────────────────────────────────────────
private val MktGradTop    = Color(0xFF1A5C6E)
private val MktGradBot    = Color(0xFF071220)
private val MktAccent     = Color(0xFF1DADC0)
private val MktWhite      = Color(0xFFFFFFFF)
private val MktGray       = Color(0xFFB0B8C8)
private val MktCardBg     = Color(0x22FFFFFF)
private val MktCardBorder = Color(0x33FFFFFF)
private val MktGreen      = Color(0xFF27AE60)
private val MktOrange     = Color(0xFFE67E22)
private val MktRed        = Color(0xFFE74C3C)

// ── Modèles ───────────────────────────────────────────────────────────────────
data class MarketOffer(
    val filmId: String = "",
    val filmTitle: String = "",
    val sellerUid: String = "",
    val sellerEmail: String = ""
)

data class IncomingRequest(
    val filmId: String = "",
    val filmTitle: String = "",
    val buyerUid: String = "",
    val buyerEmail: String = ""
)

// ── Écran marché ──────────────────────────────────────────────────────────────
@Composable
fun MarketScreen(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uid         = currentUser?.uid ?: return
    val userEmail   = currentUser.email ?: ""

    var offers            by remember { mutableStateOf<List<MarketOffer>>(emptyList()) }
    var mySeekingIds      by remember { mutableStateOf<Set<String>>(emptySet()) }
    var myPossessedIds    by remember { mutableStateOf<Set<String>>(emptySet()) }
    var incomingRequests  by remember { mutableStateOf<List<IncomingRequest>>(emptyList()) }
    var pendingRequestIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var confirmOffer      by remember { mutableStateOf<MarketOffer?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    DisposableEffect(uid) {
        val db = FirebaseDatabase.getInstance()

        val marketRef = db.getReference("market")
        val marketListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offersList    = mutableListOf<MarketOffer>()
                val incomingList  = mutableListOf<IncomingRequest>()
                val pendingIds    = mutableSetOf<String>()

                snapshot.children.forEach { filmSnap ->
                    val filmId    = filmSnap.key ?: return@forEach
                    val filmTitle = filmSnap.child("title").getValue(String::class.java) ?: ""

                    // Offres (vendeurs sauf moi)
                    filmSnap.child("sellers").children.forEach { sellerSnap ->
                        val sellerUid   = sellerSnap.key ?: return@forEach
                        if (sellerUid == uid) return@forEach
                        val sellerEmail = sellerSnap.child("email").getValue(String::class.java) ?: ""
                        offersList.add(MarketOffer(filmId, filmTitle, sellerUid, sellerEmail))
                    }

                    // Demandes reçues sur mes films
                    val amSeller = filmSnap.child("sellers").child(uid).exists()
                    if (amSeller) {
                        filmSnap.child("requests").children.forEach { reqSnap ->
                            val buyerUid   = reqSnap.key ?: return@forEach
                            val buyerEmail = reqSnap.child("email").getValue(String::class.java) ?: ""
                            incomingList.add(IncomingRequest(filmId, filmTitle, buyerUid, buyerEmail))
                        }
                    }

                    // Mes demandes envoyées
                    if (filmSnap.child("requests").child(uid).exists()) {
                        pendingIds.add(filmId)
                    }
                }

                offers           = offersList
                incomingRequests = incomingList
                pendingRequestIds = pendingIds
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

        onDispose {
            marketRef.removeEventListener(marketListener)
            myFilmsRef.removeEventListener(myFilmsListener)
        }
    }

    // ── Envoyer une demande ───────────────────────────────────────────────────
    fun sendRequest(offer: MarketOffer) {
        FirebaseDatabase.getInstance()
            .getReference("market/${offer.filmId}/requests/$uid")
            .setValue(mapOf("email" to userEmail))
        scope.launch {
            snackbarHostState.showSnackbar("Demande envoyée à ${offer.sellerEmail}")
        }
    }

    // ── Accepter une demande (vendeur) ────────────────────────────────────────
    fun acceptRequest(request: IncomingRequest) {
        val db = FirebaseDatabase.getInstance()

        // Acheteur : ajouter POSSESSED
        val buyerFilmRef = db.getReference("users/${request.buyerUid}/films/${request.filmId}")
        buyerFilmRef.get().addOnSuccessListener { snap ->
            val existingStatuses = FilmStatus.values().filter {
                snap.child("statuses").child(it.name).getValue(Boolean::class.java) == true
            }.toMutableSet()
            existingStatuses.removeAll(incompatibleWith(FilmStatus.POSSESSED))
            existingStatuses.add(FilmStatus.POSSESSED)
            val statusesMap = FilmStatus.values().associate { it.name to (it in existingStatuses) }
            buyerFilmRef.child("title").setValue(request.filmTitle)
            buyerFilmRef.child("statuses").setValue(statusesMap)
        }

        // Vendeur : retirer WANT_TO_SELL
        val sellerFilmRef = db.getReference("users/$uid/films/${request.filmId}")
        sellerFilmRef.get().addOnSuccessListener { snap ->
            val sellerStatuses = FilmStatus.values().filter {
                snap.child("statuses").child(it.name).getValue(Boolean::class.java) == true
            }.toMutableSet()
            sellerStatuses.remove(FilmStatus.WANT_TO_SELL)
            if (sellerStatuses.isEmpty()) {
                sellerFilmRef.removeValue()
            } else {
                val statusesMap = FilmStatus.values().associate { it.name to (it in sellerStatuses) }
                sellerFilmRef.child("statuses").setValue(statusesMap)
            }
        }

        // Nettoyer l'index marché
        db.getReference("market/${request.filmId}/sellers/$uid").removeValue()
        db.getReference("market/${request.filmId}/requests").removeValue()
        db.getReference("market/${request.filmId}/seekers/${request.buyerUid}").removeValue()
    }

    // ── Refuser une demande (vendeur) ─────────────────────────────────────────
    fun declineRequest(request: IncomingRequest) {
        FirebaseDatabase.getInstance()
            .getReference("market/${request.filmId}/requests/${request.buyerUid}")
            .removeValue()
    }

    // ── Dialog confirmation acheteur ──────────────────────────────────────────
    confirmOffer?.let { offer ->
        AlertDialog(
            onDismissRequest = { confirmOffer = null },
            title = { Text("Confirmer la demande", color = MktWhite, fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Souhaitez-vous demander le Blu-Ray \"${offer.filmTitle}\" à ${offer.sellerEmail} ?",
                    color = MktGray
                )
            },
            confirmButton = {
                TextButton(onClick = { sendRequest(offer); confirmOffer = null }) {
                    Text("Confirmer", color = MktAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmOffer = null }) {
                    Text("Annuler", color = MktGray)
                }
            },
            containerColor = Color(0xFF1E3A45)
        )
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

                // ── Demandes reçues (pour les vendeurs) ───────────────────────
                if (incomingRequests.isNotEmpty()) {
                    item {
                        Text(
                            "Demandes reçues",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MktOrange,
                            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                        )
                    }
                    items(incomingRequests) { request ->
                        RequestCard(
                            request   = request,
                            onAccept  = { acceptRequest(request) },
                            onDecline = { declineRequest(request) },
                            modifier  = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                // ── Offres disponibles ────────────────────────────────────────
                item {
                    Text(
                        "Offres disponibles",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MktGray,
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                    )
                }

                if (offers.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
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
                        val isPending   = offer.filmId in pendingRequestIds
                        OfferCard(
                            offer       = offer,
                            isMatch     = isMatch,
                            isPossessed = isPossessed,
                            isPending   = isPending,
                            onClaim     = { confirmOffer = offer },
                            modifier    = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Carte demande reçue ───────────────────────────────────────────────────────
@Composable
fun RequestCard(
    request: IncomingRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MktOrange.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .border(1.dp, MktOrange.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MktOrange.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.SwapHoriz, null, tint = MktOrange, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                request.filmTitle,
                color = MktWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                request.buyerEmail,
                color = MktGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onDecline, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.Close, null, tint = MktRed, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(4.dp))
        Button(
            onClick = onAccept,
            colors = ButtonDefaults.buttonColors(containerColor = MktGreen),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Accepter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Carte offre ───────────────────────────────────────────────────────────────
@Composable
fun OfferCard(
    offer: MarketOffer,
    isMatch: Boolean,
    isPossessed: Boolean,
    isPending: Boolean,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isMatch) MktAccent.copy(alpha = 0.12f) else MktCardBg,
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (isMatch) MktAccent.copy(alpha = 0.6f) else MktCardBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MktOrange.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Movie, null, tint = MktOrange, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    offer.filmTitle,
                    color = MktWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isMatch && !isPossessed && !isPending) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(MktAccent, RoundedCornerShape(50))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text("Match", color = MktWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        when {
            isPossessed -> Box(
                modifier = Modifier
                    .background(MktGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Obtenu", color = MktGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            isPending -> Box(
                modifier = Modifier
                    .background(MktOrange.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("En attente", color = MktOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            else -> Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(containerColor = MktAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Obtenir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
