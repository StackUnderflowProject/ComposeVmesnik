import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import model.FootballTeam

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import interfaces.ITeam
import kotlinx.coroutines.*
import model.Match
import model.Matches
import model.Standings
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import scrapers.KZSScraper
import scrapers.PLTScraper
import scrapers.RZSScraper
import java.io.IOException
import java.lang.reflect.InvocationTargetException
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
@Composable
fun DataOrLoadingScreen(token: String,sports: Sports) {
    var isLoading by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf<MutableList<FootballMatch>?>(null) }
    val coroutineScope = currentCompositionLocalContext

    /*DisposableEffect(Unit) {
        val job = coroutineScope.launch(Dispatchers.IO) {
            try {
                isLoading = true
                data = getMatches1()
            } catch (e: Exception) {
                println("Err: ${e.message}")
            } finally {
                isLoading = false
            }
        }
        onDispose {
            println("Canceled")
            job.cancel()
        }
    }*/
    LaunchedEffect(Unit){
        try {
        data = withContext(Dispatchers.IO){ when(sports){
            Sports.Football -> {
                val teams = PLTScraper.getTeams()
                teams.forEach{ createFootballTeam(token,Team(it?.id.toString(),it?.name ?: "","",it?.director ?: "",it?.coach ?: "",it?.logoPath ?: "",it?.season?.toInt() ?: 1))}
                val stadiums = PLTScraper.getStadiums(teams = teams)
                val matches = PLTScraper.getMatches(teams = teams, stadiums = stadiums)
                val populatedMatches = mutableListOf<FootballMatch>()
                matches.forEach { match ->
                    val  tm = teams.find { it.id == match.away }
                    val  tmH = teams.find { it.id == match.home }
                    val team = Team(tm?.id.toString(),tm?.name ?: "","",tm?.director ?: "",tm?.coach ?: "",tm?.logoPath ?: "",tm?.season?.toInt() ?: 1)
                    val teamH = Team(tmH?.id.toString(),tmH?.name ?: "","",tmH?.director ?: "",tmH?.coach ?: "",tmH?.logoPath ?: "",tmH?.season?.toInt() ?: 1)

                    val stadium = stadiums.find { it.id == match.stadium }

                    val stdm = StadiumU(_id = stadium?.id.toString(),stadium?.name ?: "",stadium?.teamId.toString(),Location("Point", listOf( stadium?.location?.lat ?: 0.0,stadium?.location?.lng ?: 0.0)),stadium?.capacity?.toInt() ?: 0,stadium?.buildYear?.toInt() ?: 0,stadium?.imagePath ?: "",stadium?.season?.toInt() ?: 0)
                    val id = createFootballStadion(token,stdm)
                    stdm._id = id ?: ""
                    populatedMatches.add(FootballMatch(match.id.toString(),parseDate(match.date.toString()) ?: Date(), match.time ?: "", teamH,team,match.score ?: "",match.location,stdm,match.season.toInt()))
                }
                populatedMatches
            }

            Sports.Handball -> {
                val teams = RZSScraper.getTeams()
                teams.forEach{ createHandballTeam(token,Team(it?.id.toString(),it?.name ?: "","",it?.director ?: "",it?.coach ?: "",it?.logoPath ?: "",it?.season?.toInt() ?: 1))}

                val stadiums = RZSScraper.getArenas(teams = teams)
                val matches = RZSScraper.getMatches(teams = teams, arenas = stadiums)
                val populatedMatches = mutableListOf<FootballMatch>()
                matches.forEach { match ->
                    val  tm = teams.find { it.id == match.away }
                    val  tmH = teams.find { it.id == match.home }
                    val team = Team(tm?.id.toString(),tm?.name ?: "","",tm?.director ?: "",tm?.coach ?: "",tm?.logoPath ?: "",tm?.season?.toInt() ?: 1)
                    val teamH = Team(tmH?.id.toString(),tmH?.name ?: "","",tmH?.director ?: "",tmH?.coach ?: "",tmH?.logoPath ?: "",tmH?.season?.toInt() ?: 1)

                    val stadium = stadiums.find { it.id == match.stadium }

                    val stdm = StadiumU(_id = stadium?.id.toString(),stadium?.name ?: "",stadium?.teamId.toString(),Location("Point", listOf( stadium?.location?.lat ?: 0.0,stadium?.location?.lng ?: 0.0)),stadium?.capacity?.toInt() ?: 0,stadium?.buildYear?.toInt() ?: 0,stadium?.imagePath ?: "",stadium?.season?.toInt() ?: 0)
                    val id = createHandballStadion(token,stdm)
                    stdm._id = id ?: ""
                    populatedMatches.add(FootballMatch(match.id.toString(),parseDate(match.date.toString()) ?: Date(), match.time ?: "", teamH,team,match.score ?: "",match.location,stdm,match.season.toInt()))
                }
                populatedMatches
            }

        } }
        }catch (e: Exception){
            println(e)
        }

        isLoading = false
    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            data?.let { matches ->
                when(sports){
                    Sports.Football -> {
                        Column {
                            Row (Modifier.fillMaxWidth(),Arrangement.Center){  Button(
                                onClick = {
                                    data?.forEach{createMatch(token,it,it.stadium._id)}
                                    data = null
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                            ) {
                                Icon(imageVector = TablerIcons.Check, contentDescription = null)
                            } }
                            LazyGrid(data ?: mutableListOf(),token,0,{},false,{matches -> data = matches})

                        }
                    }
                    Sports.Handball -> {
                        Column {
                            Row (Modifier.fillMaxWidth(),Arrangement.Center){ Button(
                                onClick = {
                                    data?.forEach{createMatchH(token,it,it.stadium._id)}
                                    data = null
                                          },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                            ) {
                                Icon(imageVector = TablerIcons.Check, contentDescription = null)
                            } }
                            LazyGridH(data ?: mutableListOf(),token,false)

                        }

                    }
                }

            } ?: Text("No Data Available", fontSize = 20.sp)


            /*LazyColumn {
                item {
                    data?.let {

                        if (it.isNotEmpty()) {
                            Button(
                                onClick = {
                                    data?.forEach { createMatchScraper(token,it,sports) }
                                    //createMatchScraper(token = token, match, sports = sports)
                                    data = listOf()
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                            ) {
                                Icon(imageVector = TablerIcons.Check, contentDescription = null)
                            }
                            it.forEach { match ->
                                MatchItem(match = match){data = data!!.toMutableList().apply {   remove(match)  } }
                            }
                        } else {
                            Text(text = "No Data Available", fontSize = 20.sp)
                        }
                    }
                }
            }*/


        }
    }
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
data class Standing(
    val _id: String,
    val place: Int,
    val team: Team,
    val gamesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsScored: Int,
    val goalsConceded: Int,
    val points: Int,
    val season: Int
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
    var _id: String,
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
    return try {
        (start..end).random()
    }
    catch (e: Exception){
        0
    }
}

fun generateMatch(
    dateMin: Date,
    dateMax: Date,
    scoreMin: Int,
    scoreMax: Int,
    seasonMin: Int,
    seasonMax: Int,
    time : String,
    sports: Sports
): FootballMatch {
    val randomDate = randomDateBetween(dateMin, dateMax)
    val randomSeason = randomIntBetween(seasonMin, seasonMax)
    val teams = if(sports == Sports.Football){fetchFootballTeams()}else{fetchHandballTeams()}
    val stadiums = if(sports == Sports.Football){fetchFootballStadiums()}else{fetchHandballStadiums()}
    val rds = stadiums!!.random()
    println(rds)
    return FootballMatch(
        _id = "",
        date = randomDate,
        score = "${randomIntBetween(scoreMin,scoreMax)}-${randomIntBetween(scoreMin,scoreMax)}",
        season = randomSeason ,
        location = "David",
        away = teams!!.random(),
        home = teams!!.random(),
        time = time,
        stadium = StadiumU(_id = rds._id, name = rds.name, buildYear = rds.buildYear, capacity = rds.capacity, imageUrl = rds.imageUrl ?: "", location =  rds.location, season =  rds.season, teamId = rds.teamId._id)
    )
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
fun StandingsScreen(sports: Sports) {
    var isLoading by remember { mutableStateOf(true) }
    var standings by remember { mutableStateOf<MutableList<Standing>?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            standings = withContext(Dispatchers.IO) {
                when (sports) {
                    Sports.Football -> fetchStandingsFootball()
                    Sports.Handball -> fetchStandingsHandball()
                }
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            standings?.let {
                StandingsContent(it)
            } ?: Text("No Data Available", fontSize = 20.sp)
        }
    }
}

@Composable
fun StandingsContent(standing: MutableList<Standing>) {
    LazyColumn {
        items(standing) { standing ->
            StandingItem(standing)
            Divider()
        }
    }
}

@Composable
fun StandingItem(standing: Standing) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(text = standing.place.toString(), modifier = Modifier.weight(1f))

        Text(text = standing.team.name, modifier = Modifier.weight(4f))
        Text(text = standing.gamesPlayed.toString(), modifier = Modifier.weight(1f))
        Text(text = standing.wins.toString(), modifier = Modifier.weight(1f))
        Text(text = standing.draws.toString(), modifier = Modifier.weight(1f))
        Text(text = standing.losses.toString(), modifier = Modifier.weight(1f))
        Text(text = standing.goalsScored.toString(), modifier = Modifier.weight(1f))
        Text(text = standing.goalsConceded.toString(), modifier = Modifier.weight(1f))
        Text(text = standing.points.toString(), modifier = Modifier.weight(1f))
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
                        LazyGrid(token = token, items = footballMatches!!, index = index, update = true, updateIndex = { num -> index = num; }, updateMatches = {})
                    }};
                    Datasets.Teams ->  LazyGrid(fetchFootballTeams(),token)
                    else -> StandingsScreen(sports = sport)}
                Sports.Handball -> when(datasets){
                    Datasets.Matches-> {
                    val handballMatches by produceState<MutableList<FootballMatch>?>(initialValue = null) { value = fetchHandballMatches((token)) }
                    if (handballMatches == null) {
                        Text("Fetching...")
                    } else {
                        LazyGridH(token = token, items = handballMatches!!, update = true)
                    }};
                    Datasets.Teams ->  LazyGridH(fetchHandballTeams(),token)
                    else -> StandingsScreen(sports = sport)
                }
            }
        }  }
    }




    }
@Composable
fun Scrapper(token: String){
    var sport by remember { mutableStateOf(Sports.values().first()) }
    var index by remember { mutableStateOf(0) }
    Column {
        Row (modifier = Modifier.padding(start = 20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround){
            DropdownSports { sp -> sport = sp}
        }


        Row(modifier = Modifier.fillMaxSize()) {

            Box( modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                when(sport){
                    Sports.Football -> DataOrLoadingScreen(token = token, sports = Sports.Football)
                    //Sports.Handball -> DataOrLoadingScreen (token = token, sports = Sports.Handball, getMatches1 = { getMatchesH() })
                    Sports.Handball -> DataOrLoadingScreen(token = token, sports = Sports.Handball)
                    }
                }
            }  }
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
        DropdownSports { sport -> sports = sport  }

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
                                time = randomIntBetween(sliderPosition.start.toInt(),sliderPosition.endInclusive.toInt()).toString(),
                                sports
                            )

                            if (sports == Sports.Football) createFootballMatch(token,match)
                            if (sports == Sports.Handball) createHandballMatch(token,match)
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
                        imageVector = TablerIcons.Pokeball,
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
                        imageVector = Icons.Filled.Edit,
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
                Tabs.Scraper -> Scrapper(token)
                Tabs.Editor -> Editor(token)

                Tabs.Generator -> Generator(token) { sport -> println(sport) }

            }
        }
}



@Composable
@Preview
fun App() {
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
    Window(onCloseRequest = ::exitApplication, title = "AdminTool", state = WindowState(size = DpSize(1000.dp,800.dp))) {
        App()
    }
}
