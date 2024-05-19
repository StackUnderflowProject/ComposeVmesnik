import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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

fun fetchFootballMatches(token : String):MutableList<FootballMatch>{
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://localhost:3000/footballMatch/")
        .build()
    println("fetch")
    try {
        val response: Response = client.newCall(request).execute()
        val json = response.body?.string() ?: ""
        val type = object : TypeToken<MutableList<FootballMatch>>() {}.type
        val gson = Gson()
        var matches : MutableList<FootballMatch> = gson.fromJson(json,type)

        return matches
    } catch (e: IOException) {
        e.printStackTrace()
        return mutableListOf()
    }
}
fun updateFootballMatch(token: String, updatedMatch: FootballMatch): Boolean {
    val client = OkHttpClient()
    val gson = Gson()
    val jsonMatch = gson.toJson(updatedMatch)
    val requestBody = jsonMatch.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/footballMatch/${updatedMatch._id}")
        .put(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    println("Updating match with ID: ${updatedMatch._id}")
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
fun deleteFootballMatch(token: String, id: String): Boolean {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("http://localhost:3000/footballMatch/$id")
        .delete()
        .addHeader("Authorization", "Bearer $token")
        .build()

    println("Deleting match with ID: $id")
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

@Composable
fun LazyGrid(items: MutableList<FootballMatch>,token: String) {
    val state = rememberLazyListState(0)
    var matches by remember { mutableStateOf(items) }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize()
        ) {
            items(matches.size) { index ->
                FMatch(token = token,matches[index], onUpdateMatch =  { updatedMatch ->
                    println(updatedMatch.score)

                    matches = matches.toMutableList().apply { this[index] = updatedMatch }
                }, deleteMatch =

                { match ->
                    matches = matches.toMutableList().apply { remove(match) }
                })
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp),
            adapter = rememberScrollbarAdapter(scrollState = state)
        )
    }
}

@Composable
fun TextFild(label: String,value: String,updateData : (String) -> Unit){
    var valueDate  = remember { mutableStateOf(value) }
    OutlinedTextField(
        value = valueDate.value,
        onValueChange = { valueDate.value = it;updateData(valueDate.value)},
        label = { Text("$label") },
        textStyle = MaterialTheme.typography.h6
    )

}
@Composable
fun FMatch(token: String,footballMatch: FootballMatch, onUpdateMatch: (FootballMatch) -> Unit,deleteMatch: (FootballMatch)->Unit) {

    var editing by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(footballMatch.date) }
    var time by remember { mutableStateOf(footballMatch.time) }
    var score by remember { mutableStateOf(footballMatch.score) }
    var location by remember { mutableStateOf(footballMatch.location) }
    var season by remember { mutableStateOf(footballMatch.season) }

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
            if (!editing) {
                Text(text = "Match ID: ${footballMatch._id}", style = MaterialTheme.typography.h6 )
                Text(text = "Date: ${date}", style = MaterialTheme.typography.h6 )
                Text(text = "Time: ${footballMatch.time}", style = MaterialTheme.typography.h6 )
                Text(text = "Home Team: ${footballMatch.home.name}", style = MaterialTheme.typography.h6 )
                Text(text = "Away Team: ${footballMatch.away.name}", style = MaterialTheme.typography.h6 )
                Text(text = "Score: ${footballMatch.score}", style = MaterialTheme.typography.h6 )
                Text(text = "Location: ${footballMatch.location}", style = MaterialTheme.typography.h6 )
                Text(text = "Season: ${footballMatch.season}", style = MaterialTheme.typography.h6 )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Button(onClick = { editing = !editing }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                    }
                    Button(onClick = {deleteFootballMatch(token,footballMatch._id);deleteMatch(footballMatch)}, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)) {
                        Icon(imageVector = TablerIcons.Trash, contentDescription = null)
                    }
                }
            } else {
                TextFild("Date", footballMatch.date){ dat -> date = dat}
                TextFild("Time", footballMatch.time){ tim -> time = tim}
                TextFild("Location", footballMatch.location) { loc -> location = loc }
                TextFild("Season", footballMatch.season.toString()) { ses -> season = ses.toInt() }
                TextFild("Score", footballMatch.score) { scr -> score = scr }


                Row(horizontalArrangement = Arrangement.SpaceAround) {
                    Button(
                        onClick = {
                            val mtch =  footballMatch.copy(
                            date = date,
                            time = time,
                            score = score,
                            season = season,
                            location = location,
                            )
                            onUpdateMatch(mtch)
                            updateFootballMatch(token = token,mtch)
                            editing = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                    ) {
                        Icon(imageVector = TablerIcons.Check, contentDescription = null)
                    }
                    Button(
                        onClick = { editing = !editing;
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                    ) {
                        Icon(imageVector = TablerIcons.X, contentDescription = null)
                    }
                }
            }
        }
    }
}