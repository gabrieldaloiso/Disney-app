package fr.isen.daloiso.disneyapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import fr.isen.daloiso.disneyapp.R
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignupScreen(navController: NavHostController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1A5C6E), Color(0xFF071220))
                )
            )
    ) {
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Image(
                    painter = painterResource(id = R.drawable.disneymoins),
                    contentDescription = "Disney Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = "Create an account",
                    modifier = Modifier.fillMaxWidth().heightIn(),
                    style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(25.dp))
                Column {
                    OutlinedTextField(
                        label = { Text(text = "First Name") },
                        value = firstName,
                        onValueChange = { firstName = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1DADC0),
                            focusedLabelColor = Color(0xFF1DADC0),
                            cursorColor = Color(0xFF1DADC0),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedLeadingIconColor = Color(0xFF1DADC0),
                            focusedTextColor = Color(0xFF1C1B1F),
                            unfocusedTextColor = Color(0xFF1C1B1F)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        leadingIcon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions.Default
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        label = { Text(text = "Last Name") },
                        value = lastName,
                        onValueChange = { lastName = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1DADC0),
                            focusedLabelColor = Color(0xFF1DADC0),
                            cursorColor = Color(0xFF1DADC0),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedLeadingIconColor = Color(0xFF1DADC0),
                            focusedTextColor = Color(0xFF1C1B1F),
                            unfocusedTextColor = Color(0xFF1C1B1F)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        leadingIcon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions.Default
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        label = { Text(text = "Email") },
                        value = email,
                        onValueChange = { email = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1DADC0),
                            focusedLabelColor = Color(0xFF1DADC0),
                            cursorColor = Color(0xFF1DADC0),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedLeadingIconColor = Color(0xFF1DADC0),
                            focusedTextColor = Color(0xFF1C1B1F),
                            unfocusedTextColor = Color(0xFF1C1B1F)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        leadingIcon = { Icon(imageVector = Icons.Outlined.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions.Default
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        label = { Text(text = "Password") },
                        value = password,
                        onValueChange = { password = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1DADC0),
                            focusedLabelColor = Color(0xFF1DADC0),
                            cursorColor = Color(0xFF1DADC0),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedLeadingIconColor = Color(0xFF1DADC0),
                            focusedTextColor = Color(0xFF1C1B1F),
                            unfocusedTextColor = Color(0xFF1C1B1F)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { checked = it },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(
                                checkedColor = Color(0xFF1DADC0),
                                uncheckedColor = Color(0xFF1DADC0)
                            )
                        )
                        Text(text = "I accept the terms and conditions", color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (errorMessage.isNotEmpty()) {
                        Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Email et mot de passe requis"
                            } else {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        navController.navigate("login")
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = e.message ?: "Erreur inconnue"
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(48.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1DADC0)
                        )
                    ) {
                        Text(text = "Register", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val annotatedString = buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(style = SpanStyle(color = Color(0xFF1DADC0), fontWeight = FontWeight.Bold)) {
                            pushStringAnnotation(tag = "Login", annotation = "Login")
                            append("Login")
                            pop()
                        }
                    }
                    ClickableText(
                        modifier = Modifier.fillMaxWidth(),
                        style = TextStyle(textAlign = TextAlign.Center, color = Color.White),
                        text = annotatedString,
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(offset, offset)
                                .firstOrNull()?.let { navController.navigate("login") }
                        }
                    )
                }
            }
        }
    }
}