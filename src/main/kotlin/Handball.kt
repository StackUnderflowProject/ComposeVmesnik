import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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


@Composable
fun LazyGridH(items: MutableList<FootballMatch>,token: String,update : Boolean) {
    val state = rememberLazyListState(0)
    var matches by remember { mutableStateOf(items) }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize()
        ) {
            items(matches.size) { index ->
                HMatch(token = token,matches[index], onUpdateMatch =  { updatedMatch ->

                    matches = matches.toMutableList().apply { this[index] = updatedMatch }
                }, deleteMatch =

                { match ->
                    matches = matches.toMutableList().apply { remove(match) }
                },update)
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 2.dp),
            adapter = rememberScrollbarAdapter(scrollState = state)
        )
    }
}

/*@Composable
fun TextFild(label: String,value: String,updateData : (String) -> Unit){
    var valueDate  = remember { mutableStateOf(value) }
    OutlinedTextField(
        value = valueDate.value,
        onValueChange = { valueDate.value = it;updateData(valueDate.value)},
        label = { Text("$label") },
        textStyle = MaterialTheme.typography.h6
    )

}*/
@Composable
fun HMatch(token: String,footballMatch: FootballMatch, onUpdateMatch: (FootballMatch) -> Unit,deleteMatch: (FootballMatch)->Unit,update: Boolean) {
    var errorMessage by remember { mutableStateOf("") }

    var dateInput by remember {  mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(footballMatch.date).toString()) }
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
                    Button(onClick = {if (update) deleteHandballMatch( token,footballMatch._id);
                        deleteMatch(footballMatch)}, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)) {
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
                            if (update) updateHandballMatch(token = token,mtch)
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
