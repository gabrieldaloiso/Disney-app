package fr.isen.daloiso.disneyapp.Screens


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import fr.isen.daloiso.disneyapp.components.BottomComponent
import fr.isen.daloiso.disneyapp.components.HeadingTextComponent
import fr.isen.daloiso.disneyapp.components.MyTextFieldComponent
import fr.isen.daloiso.disneyapp.components.NormalTextComponent
import fr.isen.daloiso.disneyapp.components.PasswordTextFieldComponent


@Composable
fun LoginScreen(navController: NavHostController?) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                NormalTextComponent(value = "Hey, there")
                HeadingTextComponent(value = "Welcome Back")
            }
            Spacer(modifier = Modifier.height(25.dp))
            Column {
                MyTextFieldComponent(labelValue = "Email", icon = Icons.Outlined.Email)
                Spacer(modifier = Modifier.height(10.dp))
                PasswordTextFieldComponent(labelValue = "Password", icon = Icons.Outlined.Lock)
            }
            BottomComponent(
                textQuery = "Don't have an account? ",
                textClickable = "Register",
                action = "Login",
                navController
            )
        }
    }
}
