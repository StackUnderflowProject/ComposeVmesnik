import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay

enum class Pages { Login, Content }



@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }
    var page by remember { mutableStateOf(Pages.Login) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (page) {
                Pages.Login -> LoginPage(onLogin =  { username, password ->
                    if (auth(username, password) { receivedToken ->
                            token = receivedToken
                        }) {
                        page = Pages.Content
                    } else {
                        toastMessage = "Invalid credentials"
                        showToast = true
                    }
                })
                Pages.Content -> {
                    Button(onClick = { text = "$token" }) {
                        Text(text)
                    }
                }
            }
            if (showToast) {
                Toast(message = toastMessage) {
                    showToast = false
                }
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
