import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch

enum class Pages { Login, Content }
enum class Tabs { Scraper,Editor,Generator}
enum class Sports{Football,Handball}
enum class Datasets{Matches,Standing,Teams}
data class FootballMatch(
    val _id: String,
    val date: String,
    val time: String,
    val home: Team,
    val away: Team,
    val score: String,
    val location: String,
    val stadium: Stadium,
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
    val teamId: String,
    val capacity: Int,
    val buildYear: Int,
    val imageUrl: String,
    val season: Int,
)



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
                    Datasets.Standing -> Row {
                        Text("Standings");Icon(
                        painter = painterResource("drawable/scoreboard.svg"),
                        contentDescription = null
                    )
                    }

                    Datasets.Matches -> Row {
                        Text("Matches");Icon(
                        imageVector = TablerIcons.Tournament,
                        contentDescription = null
                    )
                    }

                    Datasets.Teams -> Row {
                        Text("Teams");Icon(
                        painter = painterResource("drawable/shirt.svg"),
                        contentDescription = null
                    )
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



@Composable
fun DropdownSports(updateSports: (Sports) -> Unit){
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(Sports.values().first()) }
    Column {
        Button(
            onClick = { expanded = !expanded },
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when(selectedItem){
                    Sports.Football -> Row { Text("Football");Icon(imageVector = TablerIcons.BallFootball, contentDescription = null) }
                    Sports.Handball ->  Row { Text("Handball");Icon(painterResource("drawable/handball.svg"), contentDescription = null) }
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
    var datasets by remember { mutableStateOf(Datasets.values().first()) }
    Column {   Row (modifier = Modifier.padding(start = 20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround){
        DropdownDatasets { set -> datasets = set }
        DropdownSports { sp -> sport = sp}
    }
        Row {Box( modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            when(sport){
                Sports.Football -> when(datasets){Datasets.Matches-> {val footballMatches by produceState<MutableList<FootballMatch>?>(initialValue = null) {
                    value = fetchFootballMatches(token)
                }
                    if (footballMatches == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Fetching...")                        }
                    } else {
                        LazyGrid(token = token, items = footballMatches!!)
                    }};else -> Text("else")}
                else -> Text("else")
            }
        }  }
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
            drawerShape = customShape(),
            content = {
                when(tab){
                    Tabs.Scraper -> Text("Scraper")
                    Tabs.Editor -> Editor(token)

                    Tabs.Generator-> Text("Generator")

                }
            }
        )
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
