import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key.Companion.A
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.LocalDate
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

enum class Pages { Login, Content }
enum class Tabs { Scraper,Editor,Generator}
enum class Sports{Football,Handball}
enum class Datasets{Matches,Standing,Teams}
fun Float.roundTo(decimalPlaces: Int): Float {
    val factor = 10.0.pow(decimalPlaces)
    return (this * factor).roundToInt() / factor.toFloat()
}

fun ClosedFloatingPointRange<Float>.roundTo(decimalPlaces: Int): ClosedFloatingPointRange<Float> {
    val roundedStart = start.roundTo(decimalPlaces)
    val roundedEnd = endInclusive.roundTo(decimalPlaces)
    return roundedStart..roundedEnd
}
data class FootballMatch(
    val _id: String,
    val date: Date,
    val time: String,
    val home: Team,
    val away: Team,
    val score: String,
    val location: String,
    val stadium: StadiumU,
    val season: Int,
)

data class Team(
    val _id: String,
    val name: String,
    val president: String,
    val director: String,
    val coach: String,
    val logoPath: String,
    val season: Int,
)

data class Stadium(
    val _id: String,
    val name: String,
    val teamId: Team,
    val location: Location,
    val capacity: Int,
    val buildYear: Int,
    val imageUrl: String,
    val season: Int,
)

data class StadiumU(
    val _id: String,
    val name: String,
    val teamId: String,
    val location: Location,
    val capacity: Int,
    val buildYear: Int,
    val imageUrl: String,
    val season: Int,
)
data class Location(
    val type: String,
    val coordinates: List<Double>
)
fun randomDateBetween(startDate: Date, endDate: Date): Date {
    val startMillis = startDate.time
    val endMillis = endDate.time
    val randomMillis = startMillis + (Math.random() * (endMillis - startMillis)).toLong()
    return Date(randomMillis)
}

fun randomIntBetween(start: Int, end: Int): Int {
    return (start..end).random()
}

fun generateMatch(
    dateMin: Date,
    dateMax: Date,
    scoreMin: Int,
    scoreMax: Int,
    seasonMin: Int,
    seasonMax: Int,
    time : String
): FootballMatch {
    val randomDate = randomDateBetween(dateMin, dateMax)
    val randomSeason = randomIntBetween(seasonMin, seasonMax)
    val teams = fetchFootballTeams()
    val stadiums = fetchFootballStadiums()
    val rds = stadiums!!.random()
    return FootballMatch(
        _id = "",
        date = randomDate,
        score = "${randomIntBetween(scoreMin,scoreMax)}-${randomIntBetween(scoreMin,scoreMax)}",
        season = randomSeason,
        location = "David",
        away = teams!!.random(),
        home = teams!!.random(),
        time = time,
        stadium = StadiumU(rds._id,rds.name, buildYear = rds.buildYear, capacity = rds.capacity, imageUrl = rds.imageUrl, location =  rds.location, season =  rds.season, teamId = rds.teamId._id)
    )
}
fun fetchFootballStadiums():MutableList<Stadium>{
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://localhost:3000/footballStadium/")
        .build()
    try {
        val response: Response = client.newCall(request).execute()
        val json = response.body?.string() ?: ""
        val type = object : TypeToken<MutableList<Stadium>>() {}.type
        val gson = Gson()
        var matches : MutableList<Stadium> = gson.fromJson(json,type)

        return matches
    } catch (e: IOException) {
        e.printStackTrace()
        return mutableListOf()
    }
}
fun fetchStadiums(sports: Sports): List<Stadium>?
{
    var url = ""
    if (sports == Sports.Football){
        url = "http://localhost:3000/footballSatadium/"
    }
    else{
        url = "http://localhost:3000/handballStadium/"
    }
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    return try {
        val response: Response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val body = response.body?.string()
            if (!body.isNullOrEmpty()) {
                val teamListType = object : TypeToken<List<Stadium>>() {}.type
                Gson().fromJson(body, teamListType)
            } else {
                null
            }
        } else {
            println("Error: ${response.code}")
            null
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

@Composable
fun DropdownDatasets(updateDatasets: (Datasets) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(Datasets.values().first()) }
    Column {
        Button(
            onClick = { expanded = !expanded },
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (selectedItem) {
                    Datasets.Standing ->  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                        Icon(painter = painterResource("drawable/scoreboard.svg"), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Standings")
                        IconButton(
                            onClick = {expanded = !expanded},
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    }
                    Datasets.Matches ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                            Icon(imageVector = TablerIcons.Tournament, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Matches")
                            IconButton(
                                onClick = {expanded = !expanded},
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }
                    Datasets.Teams ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                            Icon(painter = painterResource("drawable/shirt.svg"), contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Teams")
                            IconButton(
                                onClick = {expanded = !expanded},
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(12.dp,(-11).dp)
        ) {
            DropdownMenuItem(onClick = {
                selectedItem = Datasets.Matches
                updateDatasets(Datasets.Matches)
                expanded = false
            }) {
                Row {
                    Text("Matches")
                    Icon(imageVector = TablerIcons.Tournament, contentDescription = null)
                }
            }
                DropdownMenuItem(onClick = {
                    selectedItem = Datasets.Teams
                    updateDatasets(Datasets.Teams)
                    expanded = false
                }) {
                    Row {
                        Text("Teams")
                        Icon(painter = painterResource("drawable/shirt.svg"), contentDescription = null)
                    }
                }
                DropdownMenuItem(onClick = {
                    selectedItem = Datasets.Standing
                    updateDatasets(Datasets.Standing)
                    expanded = false
                }) {
                    Row {
                        Text("Standings")
                        Icon(painter = painterResource("drawable/scoreboard.svg"), contentDescription = null)
                    }
                }
            }

        }

    }


val LeftRoundedCornerShape: Shape = RoundedCornerShape(
    topStart = 6.dp,
    topEnd = 0.dp,
    bottomEnd = 0.dp,
    bottomStart = 6.dp
)

val RightRoundedCornerShape: Shape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 6.dp,
    bottomEnd = 6.dp,
    bottomStart = 0.dp
)


@Composable
fun DropdownSports(updateSports: (Sports) -> Unit){
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(Sports.values().first()) }
    Column {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 0.dp),
            ) {
                when (selectedItem) {
                    Sports.Football ->  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                        Icon(imageVector = TablerIcons.BallFootball, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Football")
                        IconButton(
                            onClick = {expanded = !expanded},
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    }

                    Sports.Handball -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                        Icon(painter = painterResource("drawable/handball.svg"), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Handball")
                        IconButton(
                            onClick = {expanded = !expanded},
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    }
                }
            }

        }



        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(12.dp,(-11).dp)

        ) {
            DropdownMenuItem(onClick = {
                selectedItem = Sports.Football
                updateSports(Sports.Football)
                expanded = false
            }) {
                Row {
                    Text("Football");Icon(imageVector = TablerIcons.BallFootball, contentDescription = null)                        }
            }
            DropdownMenuItem(onClick = {
                selectedItem = Sports.Handball
                updateSports(Sports.Handball)

                expanded = false
            }) {
                Row {
                    Text("Handball");Icon(painterResource("drawable/handball.svg"), contentDescription = null)
            }

        }

    }

}
}



@Composable
fun Editor(token: String){
    var sport by remember { mutableStateOf(Sports.values().first()) }
    var index by remember { mutableStateOf(0) }
    var datasets by remember { mutableStateOf(Datasets.values().first()) }
    Column {
        Row (modifier = Modifier.padding(start = 20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround){
        DropdownDatasets { set -> datasets = set }
        DropdownSports { sp -> sport = sp}
    }


        Row(modifier = Modifier.fillMaxSize()) {

            Box( modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            when(sport){
                Sports.Football -> when(datasets){Datasets.Matches-> {
                    val footballMatches by produceState<MutableList<FootballMatch>?>(initialValue = null) { value = fetchFootballMatches(token) }
                    if (footballMatches == null) {
                        Text("Fetching...")
                    } else {
                        LazyGrid(token = token, items = footballMatches!!, index = index){num -> index = num; }
                    }};
                    Datasets.Teams ->  LazyGrid(fetchFootballTeams(),token)
                    else -> Text("else")}
                Sports.Handball -> when(datasets){
                    Datasets.Matches-> {
                    val handballMatches by produceState<MutableList<FootballMatch>?>(initialValue = null) { value = fetchHandballMatches((token)) }
                    if (handballMatches == null) {
                        Text("Fetching...")
                    } else {
                        LazyGridH(token = token, items = handballMatches!!)
                    }};
                    Datasets.Teams ->  LazyGridH(fetchHandballTeams(),token)

                    else ->Text("else")
                }
            }
        }  }
    }




    }
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Generator(token: String,onGenerateClick: (Sports)-> Unit) {
    var number by remember { mutableStateOf(1) }
    var datasets by remember { mutableStateOf(Datasets.values().first()) }
    var sports by remember { mutableStateOf(Sports.values().first()) }
    var dateMin by remember { mutableStateOf(Date()) }
    var dateMinInput by remember { mutableStateOf("") }
    var dateMax by remember { mutableStateOf(Date(0,0,0)) }
    var dateMaxInput by remember { mutableStateOf("") }
    var scoreMin by remember { mutableStateOf("") }
    var scoreMax by remember { mutableStateOf("") }
    var seasonMin by remember { mutableStateOf("") }
    var seasonMax by remember { mutableStateOf("") }
    var sliderPosition by remember { mutableStateOf(0f..24f) }


    Column(modifier = Modifier.padding(16.dp)) {
        // Row for dropdowns

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = if (number != 0){number.toString()}else "",
                    onValueChange = {
                        number = it.toIntOrNull() ?: if (it.isEmpty() ){0}else number
                    },
                    label = { Text("Number of generated data") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Date range inputs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = dateMinInput,
                    onValueChange = {
                        dateMinInput = it
                        val parsedDate = parseDate(it)
                        if (parsedDate != null) {
                            dateMin = parsedDate
                        }
                    },
                    label = { Text("Date Min (YYYY-MM-DD)") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = dateMaxInput,
                    onValueChange = {
                        dateMaxInput = it
                        val parsedDate = parseDate(it)
                        if (parsedDate != null) {
                            dateMax = parsedDate
                        }
                    },
                    label = { Text("Date Max (YYYY-MM-DD)") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Score range inputs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = scoreMin,
                    onValueChange = { scoreMin = it },
                    label = { Text("Score Min") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = scoreMax,
                    onValueChange = { scoreMax = it },
                    label = { Text("Score Max") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Season range inputs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = seasonMin,
                    onValueChange = { seasonMin = it },
                    label = { Text("Season Min") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = seasonMax,
                    onValueChange = { seasonMax = it },
                    label = { Text("Season Max") },
                    textStyle = MaterialTheme.typography.h6,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }

        }
        Row (modifier = Modifier.fillMaxWidth()){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RangeSlider(
                    value = sliderPosition,
                    onValueChange = { range ->
                        sliderPosition = range.start.roundToInt().toFloat()..range.endInclusive.roundToInt().toFloat()
                    },
                    valueRange = 0f..24f,
                    steps = 23
                )

                Text(text = "${sliderPosition.start.toInt().toString()} - ${sliderPosition.endInclusive.toInt().toString()}")
            }
        }
        Row (modifier = Modifier.fillMaxWidth()){
            Column(modifier = Modifier.padding(16.dp)) {

                Button(
                    onClick = {
                        onGenerateClick(Sports.Football);
                        println(dateMinInput)
                        val parsedDateMin = parseDate(dateMinInput) ?: Date()
                        val parsedDateMax = parseDate(dateMaxInput) ?: Date()
                        val minScore = scoreMin.toIntOrNull() ?: 0
                        val maxScore = scoreMax.toIntOrNull() ?: 0
                        val minSeason = seasonMin.toIntOrNull() ?: 0
                        val maxSeason = seasonMax.toIntOrNull() ?: 0
                        for(i in 0 ..< number ){
                            val match = generateMatch(
                                dateMin = parsedDateMin,
                            dateMax = parsedDateMax,
                            scoreMin = minScore,
                            scoreMax = maxScore,
                            seasonMin = minSeason,
                            seasonMax = maxSeason,
                                time = randomIntBetween(sliderPosition.start.toInt(),sliderPosition.endInclusive.toInt()).toString()
                            )
                            createFootballMatch(token,match)
                        }
                              },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                ) {
                    Text("Generate")
                }
            }
        }
    }

}
@Composable
fun Sidebar(updateTab : (Tabs) -> Unit ,updateScaffoldState: () -> Unit){
    Box(contentAlignment = Alignment.CenterStart) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(200.dp)
                .fillMaxHeight()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {updateTab(Tabs.Generator);updateScaffoldState()}, colors = ButtonDefaults.buttonColors(),) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Text(text = "Generator", style = MaterialTheme.typography.h6)

                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {updateTab(Tabs.Scraper);updateScaffoldState()}, colors = ButtonDefaults.buttonColors(),) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Text(text = "Scraper", style = MaterialTheme.typography.h6)

                }

            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {updateTab(Tabs.Editor);updateScaffoldState()}, colors = ButtonDefaults.buttonColors()) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Text(text = "Editor", style = MaterialTheme.typography.h6)

                }
            }
        }


    }
}
@Composable
fun MyTopBar(openDrawer: () -> Unit) {
    TopAppBar(
        title = { Text(text = "My Application") },
        navigationIcon = {
            IconButton(onClick = openDrawer) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = 4.dp
    )
}
fun customShape() =  object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rounded(RoundRect(Rect(0f,0f,170f,200f)))
    }
}

@Composable
fun Content(token: String) {
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()
        var tab by remember { mutableStateOf(Tabs.Editor) }
        Scaffold(

            scaffoldState = scaffoldState,
            topBar = {
                MyTopBar(openDrawer = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.open()
                    }
                })
            },
            drawerContent = {
                    Sidebar(updateTab =  { tabi -> tab = tabi } , updateScaffoldState = { coroutineScope.launch { scaffoldState.drawerState.close()}})
            },
            drawerGesturesEnabled = true,
            drawerShape = customShape()
        ) {
            when (tab) {
                Tabs.Scraper -> Text("Scraper")
                Tabs.Editor -> Editor(token)

                Tabs.Generator -> Generator(token, { sport -> println(sport) })

            }
        }
}



@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }
    var page by remember { mutableStateOf(Pages.Login) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    MaterialTheme (typography =Typography() ){
        /*TopAppBar(
            title = { Text("My App") },
            navigationIcon = {
                IconButton(onClick = { /* Handle navigation icon click */ }) {
                    Icon(
                        imageVector = FontAwesomeIcons.Regular.Image, // Replace with your icon resource
                        contentDescription = "Menu"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Handle search icon click */ }) {
                    Icon(
                        imageVector = FontAwesomeIcons.Regular.Image, // Replace with your icon resource
                        contentDescription = "Search"
                    )
                }
                IconButton(onClick = { /* Handle more icon click */ }) {
                    Icon(
                        imageVector = FontAwesomeIcons.Regular.Image, // Replace with your icon resource
                        contentDescription = "More"
                    )
                }
            },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            elevation = 4.dp
        )*/
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
                    Content(token)
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
@Composable
fun Typography():androidx.compose.material.Typography {
    return Typography(
        h1 = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        ),
        h6 = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
        ),
    )
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "AdminTool") {
        App()
    }
}
