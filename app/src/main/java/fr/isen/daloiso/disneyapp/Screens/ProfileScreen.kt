package fr.isen.daloiso.disneyapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

private val BgGradient    = Brush.linearGradient(listOf(Color(0xFF1A5C6E), Color(0xFF071220)))
private val AccentColor   = Color(0xFF1DADC0)
private val CardBgDeep    = Color(0xFF122533)
private val Light         = Color(0xFFF5F5F5)

@Composable
fun ProfileScreen(navController: NavHostController?) {
    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.displayName ?: "Utilisateur"
    val email = user?.email ?: "Email non disponible"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(AccentColor, shape = CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Text(text = displayName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = email, fontSize = 14.sp, color = Light.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "MA COLLECTION",
                color = AccentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            val ownedFilms = listOf("Star Wars : A New Hope", "The Lion King", "Avengers")

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ownedFilms) { film ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBgDeep),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = film, color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))


                            Button(
                                onClick = { /* Action Firebase */ },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("VENDRE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController?.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Se déconnecter", color = Color.White)
            }
        }
    }
}