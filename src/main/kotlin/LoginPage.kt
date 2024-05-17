import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType

import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
fun isPasswordValid(password: String,updateRestrictions:(String)-> Unit): Boolean {
    val minLength = 8
    val containsUpperCase = "[A-Z]".toRegex().containsMatchIn(password)
    val containsLowerCase = "[a-z]".toRegex().containsMatchIn(password)
    val containsDigit = "\\d".toRegex().containsMatchIn(password)
    val containsSpecialChar = "[^A-Za-z0-9]".toRegex().containsMatchIn(password)

    val errorMessage = StringBuilder()

    if (password.length < minLength) {
        errorMessage.append("Password must be at least $minLength characters long.\n")
    }
    if (!containsUpperCase) {
        errorMessage.append("Password must contain at least one uppercase letter.\n")
    }
    if (!containsLowerCase) {
        errorMessage.append("Password must contain at least one lowercase letter.\n")
    }
    if (!containsDigit) {
        errorMessage.append("Password must contain at least one digit.\n")
    }
    if (!containsSpecialChar) {
        errorMessage.append("Password must contain at least one special character.\n")
    }
    println(errorMessage.toString())
    updateRestrictions(errorMessage.toString())
    return password.length >= minLength &&
            containsUpperCase &&
            containsLowerCase &&
            containsDigit &&
            containsSpecialChar

}
@Composable
fun LoginPage(onLogin: (String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(true) }
    var restrictions by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize(0.75f)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login", style = MaterialTheme.typography.h4)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Filled.Person,"user") },

            )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it;showError = !isPasswordValid(password) { res -> restrictions = res} },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    if (passwordVisible) Icon(imageVector = Icons.Filled.Settings, contentDescription = "hide")
                    else Icon(Icons.Filled.Settings, contentDescription = "show")
                }
            },
            leadingIcon = { Icon(Icons.Filled.Lock,"pass") },
            isError = showError,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                errorBorderColor = Color.Red
            ),

        )
        if (showError) {
        Text(
            text = restrictions,
            color = Color.Red,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }


        Spacer(modifier = Modifier.height(16.dp))
        if (!showError)Button(onClick = { onLogin(username, password) }) {
            Text("Login")
        }
    }
}
@Composable
fun Toast(message: String, onDismiss: () -> Unit) {
    LaunchedEffect(message) {
        delay(2000)
        onDismiss()
    }
    Box(
        modifier = Modifier.fillMaxSize(0.75f),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            backgroundColor = Color.Red,
            contentColor = Color.White,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp
            )
        }
    }
}


data class Data(
    val token: String,
)
fun auth(username:String,password:String,tokenUpdate:  (String) -> Unit): Boolean{
    val client = OkHttpClient()

    val requestBody = FormBody.Builder()
        .add("username", username)
        .add("password", password)
        .build()
    val request = Request.Builder()
        .url("http://localhost:3000/users/login")
        .post(requestBody)
        .build()


    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val responseBody = response.body?.string()
             tokenUpdate(Gson().fromJson(responseBody,Data::class.java).token)
            return true
        } else {
            println("Request failed: ${response.message}")
            return false
        }
    } catch (e: IOException) {
        return false
        println("Request failed: ${e.message}")
    }

}