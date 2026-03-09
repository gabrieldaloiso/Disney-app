package fr.isen.daloiso.disneyapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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

@Composable
fun SignupScreen(navController: NavHostController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }

    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Hello There!",
                modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Create an account",
                modifier = Modifier.fillMaxWidth().heightIn(),
                style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(25.dp))
            Column {
                OutlinedTextField(
                    label = { Text(text = "First Name") },
                    value = firstName,
                    onValueChange = { firstName = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6650A4),
                        focusedLabelColor = Color(0xFF6650A4),
                        cursorColor = Color(0xFF6650A4),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedLeadingIconColor = Color(0xFF6650A4),
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
                        focusedBorderColor = Color(0xFF6650A4),
                        focusedLabelColor = Color(0xFF6650A4),
                        cursorColor = Color(0xFF6650A4),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedLeadingIconColor = Color(0xFF6650A4),
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
                        focusedBorderColor = Color(0xFF6650A4),
                        focusedLabelColor = Color(0xFF6650A4),
                        cursorColor = Color(0xFF6650A4),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedLeadingIconColor = Color(0xFF6650A4),
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
                        focusedBorderColor = Color(0xFF6650A4),
                        focusedLabelColor = Color(0xFF6650A4),
                        cursorColor = Color(0xFF6650A4),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedLeadingIconColor = Color(0xFF6650A4),
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
                    Checkbox(checked = checked, onCheckedChange = { checked = it })
                    Text(text = "I accept the terms and conditions")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().heightIn(48.dp)
                ) {
                    Text(text = "Register", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                val annotatedString = buildAnnotatedString {
                    append("Already have an account? ")
                    withStyle(style = SpanStyle(color = Color(0xFF6650A4), fontWeight = FontWeight.Bold)) {
                        pushStringAnnotation(tag = "Login", annotation = "Login")
                        append("Login")
                        pop()
                    }
                }
                ClickableText(
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(textAlign = TextAlign.Center, color = Color.Black),
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
