import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Trash
import compose.icons.tablericons.X
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.swing.text.TableView

fun fetchFootballTeams():MutableList<Team>{
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://localhost:3000/footballTeam/")
        .build()
    try {
        val response: Response = client.newCall(request).execute()
        val json = response.body?.string() ?: ""
        val type = object : TypeToken<MutableList<Team>>() {}.type
        val gson = Gson()
        var matches : MutableList<Team> = gson.fromJson(json,type)

        return matches
    } catch (e: IOException) {
        e.printStackTrace()
        return mutableListOf()
    }
}
fun deleteFootballTeam(token: String, id: String): Boolean {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("http://localhost:3000/footballTeam/$id")
        .delete()
        .addHeader("Authorization", "Bearer $token")
        .build()

    println("Deleting team with ID: $id")
    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Delete successful: ${response.body?.string()}")
            true
        } else {
            println("Delete failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
fun updateFootballTeam(token: String, updatedTeam: Team): Boolean {
    val client = OkHttpClient()
    val gson = Gson()
    val jsonTeam = gson.toJson(updatedTeam)
    val requestBody = jsonTeam.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/footballTeam/${updatedTeam._id}")
        .put(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    println("Updating team with ID: ${updatedTeam._id}")
    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Update successful: ${response.body?.string()}")
            true
        } else {
            println("Update failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
@Composable
fun LazyGrid(items: MutableList<Team>,token: String) {
    val state = rememberLazyListState(0)
    var matches by remember { mutableStateOf(items) }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize()
        ) {
            items(matches.size) { index ->
               Row {  FootballTeam(token,items[index], { updatedTeam ->

                   matches = matches.toMutableList().apply { this[index] = updatedTeam }
               },
                   { match ->
                       matches = matches.toMutableList().apply { remove(match) }
                   }   )}
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp),
            adapter = rememberScrollbarAdapter(scrollState = state)
        )
    }
}

@Composable
fun FootballTeam(token: String, team: Team, onUpdateTeam: (Team) -> Unit, onDeleteTeam: (Team) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(team.name) }
    var coach by remember { mutableStateOf(team.coach) }
    var director by remember { mutableStateOf(team.director) }
    var president by remember { mutableStateOf(team.president) }
    var season by remember { mutableStateOf(team.season) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!isEditing) {
                Text("Name: $name", style = MaterialTheme.typography.h6)
                Text("Coach: $coach", style = MaterialTheme.typography.h6)
                Text("Director: $director", style = MaterialTheme.typography.h6)
                Text("President: $president", style = MaterialTheme.typography.h6)
                Text("Season: $season", style = MaterialTheme.typography.h6)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Button(onClick = { isEditing = true }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                    }
                   /* Button(onClick = { deleteFootballTeam(token,team._id);onDeleteTeam(team)}, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Delete")
                    }*/
                }
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    textStyle = MaterialTheme.typography.h6,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = coach,
                    onValueChange = { coach = it },
                    label = { Text("Coach") },
                    textStyle = MaterialTheme.typography.h6,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = director,
                    onValueChange = { director = it },
                    label = { Text("Director") },
                    textStyle = MaterialTheme.typography.h6,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = president,
                    onValueChange = { president = it },
                    label = { Text("President") },
                    textStyle = MaterialTheme.typography.h6,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = if (season != 0){season.toString()}else "",
                    onValueChange = { season = it.toIntOrNull() ?: if (it.isEmpty() ){0}else season },

                    label = { Text("Season") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.SpaceAround) {
                    Button(
                        onClick = {
                            val updatedTeam = team.copy(
                                name = name,
                                coach = coach,
                                director = director,
                                president = president,
                                season = season
                            )
                            onUpdateTeam(updatedTeam)
                            updateFootballTeam(token,updatedTeam)
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                    }
                    Button(
                        onClick = { isEditing = false },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            }
        }
    }
}
