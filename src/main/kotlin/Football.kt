import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
fun parseDate(dateString: String): java.util.Date? {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.isLenient = false
        return format.parse(dateString)
    } catch (e: Exception) {
        null
    }
}
fun createFootballMatch(token: String, match: FootballMatch): Boolean {
    val client = OkHttpClient()

    val gson = Gson()
    val jsonMatch = gson.toJson(match)
    val requestBody: RequestBody = jsonMatch.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/footballMatch")
        .post(requestBody)
        .addHeader("Authorization", "Bearer $token")
        .build()

    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            println("Create successful: ${response.body?.string()}")
            true
        } else {
            println("Create failed: ${response.code}")
            false
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}fun fetchFootballMatches(token : String):MutableList<FootballMatch>{
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
fun EditStadiumDropdownMenu(
    stadium: Stadium,
    onStadiumUpdated: (Stadium) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(stadium.name) }
    var capacity by remember { mutableStateOf(stadium.capacity.toString()) }
    var locationType by remember { mutableStateOf(stadium.location.type) }
    var coordinates by remember { mutableStateOf(stadium.location.coordinates.joinToString(", ")) }
    var buildYear by remember { mutableStateOf(stadium.buildYear.toString()) }
    var imageUrl by remember { mutableStateOf(stadium.imageUrl) }
    var season by remember { mutableStateOf(stadium.season.toString()) }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Button(onClick = {expanded = true}){
                Text("Stadium")
                Icon(imageVector = Icons.Filled.ArrowDropDown,null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacity") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = locationType,
                        onValueChange = { locationType = it },
                        label = { Text("Location Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = coordinates,
                        onValueChange = { coordinates = it },
                        label = { Text("Coordinates (comma separated)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = buildYear,
                        onValueChange = { buildYear = it },
                        label = { Text("Build Year") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = season,
                        onValueChange = { season = it },
                        label = { Text("Season") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val updatedStadium = stadium.copy(
                                name = name,
                                capacity = capacity.toIntOrNull() ?: stadium.capacity,
                                location = Location(
                                    type = locationType,
                                    coordinates = coordinates.split(",").mapNotNull { it.trim().toDoubleOrNull() }
                                ),
                                buildYear = buildYear.toIntOrNull() ?: stadium.buildYear,
                                imageUrl = imageUrl,
                                season = season.toIntOrNull() ?: stadium.season
                            )
                            onStadiumUpdated(updatedStadium)
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }



@Composable
fun LazyGrid(items: MutableList<FootballMatch>,token: String,index :Int,updateIndex: (Int)-> Unit) {
    var matches by remember { mutableStateOf(items) }
    var scrState = rememberLazyListState(index)
    LaunchedEffect(scrState.firstVisibleItemIndex){
        updateIndex(scrState.firstVisibleItemIndex)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrState
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
            adapter = rememberScrollbarAdapter(scrollState = scrState),

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
    var errorMessage by remember { mutableStateOf("") }

    var dateInput by remember {  mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(footballMatch.date).toString()) }
    var editing by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(footballMatch.date) }
    var time by remember { mutableStateOf(footballMatch.time) }
    var score by remember { mutableStateOf(footballMatch.score) }
    var location by remember { mutableStateOf(footballMatch.location) }
    var season by remember { mutableStateOf(footballMatch.season) }
    var stadium by remember { mutableStateOf(footballMatch.stadium) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        backgroundColor = Color.White,
        elevation = 8.dp,

    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!editing) {
                Text(text = "Match ID: ${footballMatch._id}", style = MaterialTheme.typography.h6 )
                Text(text = "Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)}", style = MaterialTheme.typography.h6 )
                Text(text = "Time: ${footballMatch.time}", style = MaterialTheme.typography.h6 )
                //Text(text = "Home Team: ${footballMatch.home ?: "a"}", style = MaterialTheme.typography.h6 )
                //Text(text = "Away Team: ${footballMatch.away.name}", style = MaterialTheme.typography.h6 )
                Text(text = "Stadium coordinates: ${footballMatch.stadium.location.coordinates}", style = MaterialTheme.typography.h6 )

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
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = {
                        dateInput = it
                        val parsedDate = parseDate(it)
                        if (parsedDate != null) {
                            date = parsedDate
                            errorMessage = ""
                        } else {
                            errorMessage = "Invalid date format. Use YYYY-MM-DD."
                        }
                    },
                    label = { Text("Date") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    isError = errorMessage.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                //TextFild("Date", footballMatch.date.toString()){ dat -> date = Date.valueOf(dat)}
                TextFild("Time", footballMatch.time){ tim -> time = tim}
                TextFild("Location", footballMatch.location) { loc -> location = loc }
                OutlinedTextField(
                    value = if (season != 0){season.toString()}else "",
                    onValueChange = { season = it.toIntOrNull() ?: if (it.isEmpty() ){0}else season },

                    label = { Text("Season") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                )
                TextFild("Score", footballMatch.score) { scr -> score = scr }
                //EditStadiumDropdownMenu(stadium) { stm -> stadium = stm }

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
