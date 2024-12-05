package com.example.tpms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.tpms.ui.theme.TpmsTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var tempRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        // Initialize Firebase Database reference
        val database = FirebaseDatabase.getInstance()
        tempRef = database.getReference("sensorData") // Reference to "sensorData" node

        createNotificationChannel()

        setContent {
            ChangeSystemBarColor(
                statusBarColor = Color(0xFF1976D2),
                navigationBarColor = Color(0xFF1976D2)
            )
            TpmsTheme {
                Tpms(tempRef = tempRef)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Temperature Alerts"
            val descriptionText = "Channel for temperature alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("TEMP_ALERT_CHANNEL", channelName, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkTemperatureAndNotify(temp: Float) {
        if (temp < 60) {
            sendTemperatureAlert(this)
        }
    }
}

//has NavHost
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tpms(tempRef: DatabaseReference) {
    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(drawerState = drawerState, navController = navController)
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF1976D2),
                        titleContentColor = Color.White,
                        scrolledContainerColor = Color(0xFF1976D2)
                    ),
                    title = {
                        Text(
                            "TPMS",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                if (drawerState.isOpen) {
                                    drawerState.close()
                                } else {
                                    drawerState.open()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open Drawer"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },

        ) { innerPadding ->
            NavHost(navController = navController, startDestination = "home") {
                composable("dashboard") {
                    val context = LocalContext.current
                    CardListScreen(
                        modifier = Modifier.padding(innerPadding),
                        tempRef = tempRef,
                        context = context
                    )
                }
                composable("map") {
                    MapScreen(tempRef = tempRef)
                }
                composable("maintenance") {
                    MaintenanceScreen(innerPadding = innerPadding, navController = navController)
                }
                composable("home") {
                    Home(navController = navController) // Add the Home composable here
                }
                composable("tyre") {
                    TyreScreen()
                }
                composable("tkph") {
                    Tkph(innerPadding = innerPadding, navController = navController)
                }
                composable("shift") {
                    ShiftHR(innerPadding = innerPadding)
                }
                composable("speed") {
                    speed(innerPadding = innerPadding)
                }
                composable("login"){
                    Login()
                }
            }
        }
    }
}

data class MaintenanceItem(
    val heading: String,
    val description: String,
    val iconResId: Int,
    val route: String
)

@Composable
fun MaintenanceScreen(innerPadding: PaddingValues, navController: NavController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val cardModifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(18.dp)
            .shadow(4.dp, shape = MaterialTheme.shapes.medium) // Add shadow

        val cardColors = CardDefaults.cardColors(containerColor = Color.White) // Set card color to white

        val items = listOf(
            MaintenanceItem(
                heading = "Tyre",
                description = "Monitor the temperature and pressure of all your vehicle's tyres in real-time.",
                iconResId = R.drawable.img, // Replace with your image resource
                route = "tyre"
            ),
            MaintenanceItem(
                heading = "TKPH",
                description = "Track the wear and tear on your tyres by measuring the Tyre-Kilometers Per Hour.",
                iconResId = R.drawable.img_3, // Replace with your image resource
                route = "tkph"
            ),
            MaintenanceItem(
                heading = "Speed",
                description = "Keep an eye on your vehicle's speed with real-time updates.",
                iconResId = R.drawable.img_2, // Replace with your image resource
                route = "speed"
            ),
            MaintenanceItem(
                heading = "GPS",
                description = "Utilize GPS tracking to monitor your vehicle's location and route",
                iconResId = R.drawable.gps, // Replace with your image resource
                route = "map"
            ),
            MaintenanceItem(
                heading = "Shift Hour",
                description = "Track the duration of each driving shift. Analyze driving patterns and manage your driving hours effectively for better tyre maintenance and overall vehicle performance",
                iconResId = R.drawable.shift, // Replace with your image resource
                route = "shift"
            )
        )

        items.forEach { item ->
            Card(
                modifier = cardModifier.clickable {
                    navController.navigate(item.route)
                },
                colors = cardColors
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(36.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = item.iconResId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .padding(end = 16.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.heading,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}




//tyre inside maintenanceScreen
@Composable
fun TyreScreen() {
    var sensorData by remember { mutableStateOf(SensorData()) }
    val isFirebaseDataAvailable = remember { mutableStateOf(false) }

    // Firebase Database reference
    val database = FirebaseDatabase.getInstance().getReference("sensorData")

    // Fetch data from Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(SensorData::class.java)
                if (data != null) {
                    sensorData = data
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // Use mock data if Firebase data isn't available
//    if (!isFirebaseDataAvailable.value) {
//        sensorData = SensorData(
//            temperature = 65f, // Example mock temperature
//            pressure = 35f     // Example mock pressure
//        )
//    }
    // Render UI
    TyreScreenContent(sensorData)
}

@Composable
fun TyreScreenContent(sensorData: SensorData) {
    val temperature = sensorData.temperature ?: 0f
    val pressure = sensorData.pressure ?: 0f

    // Define the color for temperature and pressure charts
    val temperatureColor = getColorForTemperature(temperature)
    val pressureColor = getColorForPressure(pressure)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 76.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Temperature Section
        Text(text = "Temperature", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 10.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DonutChartWithLabel(
                    value = temperature,
                    color = temperatureColor,
                    label = "Front Left Tyre"
                )
                DonutChartWithLabel(
                    value = temperature,
                    color = temperatureColor,
                    label = "Front Right Tyre"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DonutChartWithLabel(
                    value = temperature,
                    color = temperatureColor,
                    label = "Back Left Tyre"
                )
                DonutChartWithLabel(
                    value = temperature,
                    color = temperatureColor,
                    label = "Back Right Tyre"
                )
            }
        }

        // Pressure Section
        Text(text = "Pressure", style = MaterialTheme.typography.headlineMedium)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DonutChartWithLabel(
                    value = pressure,
                    color = pressureColor,
                    label = "Front Left Tyre"
                )
                DonutChartWithLabel(
                    value = pressure,
                    color = pressureColor,
                    label = "Front Right Tyre"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DonutChartWithLabel(
                    value = pressure,
                    color = pressureColor,
                    label = "Back Left Tyre"
                )
                DonutChartWithLabel(
                    value = pressure,
                    color = pressureColor,
                    label = "Back Right Tyre"
                )
            }
        }
    }
}

@Composable
fun DonutChartWithLabel(
    value: Float,
    maxValue: Float = 100f,
    color: Color,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
        // Label for the tyre position
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))

        // Donut chart with the value in the center
        Box(contentAlignment = Alignment.Center) {
            DonutChart(value = value, maxValue = maxValue, color = color)
            Text(text = "${value.toInt()}%", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun DonutChart(
    value: Float,
    maxValue: Float = 100f,
    color: Color
) {
    val sweepAngle = (value / maxValue) * 360f

    Canvas(modifier = Modifier.size(100.dp)) {
        // Background circle (empty)
        drawCircle(
            color = Color.LightGray,
            radius = size.minDimension / 2,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )

        // Foreground circle (filled)
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )
    }
}

fun getColorForTemperature(temp: Float): Color {
    return when {
        temp < 20 -> Color.Cyan
        temp < 40 -> Color.Green
        temp < 60 -> Color.Yellow
        else -> Color.Red
    }
}

fun getColorForPressure(pressure: Float): Color {
    return when {
        pressure < 30 -> Color.Cyan
        pressure < 50 -> Color.Green
        pressure < 70 -> Color.Yellow
        else -> Color.Red
    }
}



//speed inside maintenanceScreen
@Composable
fun speed(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center content vertically
    ) {
        // Image at the top
        Image(
            painter = painterResource(id = R.drawable.img_11), // Replace with your image resource
            contentDescription = "Speed Icon",
            modifier = Modifier
                .size(120.dp) // Adjust the size as needed
                .padding(bottom = 16.dp) // Add spacing below the image
        )

        // Speed text
        Text(
            text = "Current Speed: 75 km/h", // Dynamic data can be used here
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF1976D2), // Accent color for the text
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Optional description or additional details
        Text(
            text = "Maintain a safe speed while driving. Speed limits vary by location.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center, // Center-align the text
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp)) // Add extra space at the bottom if needed
    }
}


//tkph inside maintenanceScreen
@Composable
fun Tkph(innerPadding: PaddingValues, navController: NavController) {
    val scrollState = rememberScrollState()
    var tkphValue by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        val cardModifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp)
            .shadow(8.dp, shape = MaterialTheme.shapes.large) // Enhanced shadow

        val cardColors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background // Background color
        )

        // Firebase Database reference
        val database = FirebaseDatabase.getInstance().getReference("sensorData")
        var sensorData by remember { mutableStateOf(SensorData()) }
        var isDataLoaded by remember { mutableStateOf(false) }

        // Fetch data from Firebase
        LaunchedEffect(Unit) {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(SensorData::class.java)
                    if (data != null) {
                        sensorData = data
                        isDataLoaded = true
                    } else {
                        isDataLoaded = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    isDataLoaded = false
                }
            })
        }

        val loadedValue = if (isDataLoaded) sensorData.loaded?.toString() ?: "No data" else "No data"
        val unloadValue = if (isDataLoaded) sensorData.unload?.toString() ?: "No data" else "No data"

        // Display the Loaded and Unload values in cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Loaded Card
            Card(
                modifier = cardModifier,
                colors = cardColors,
                shape = MaterialTheme.shapes.medium // Rounded corners
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_7), // Replace with your image resource
                        contentDescription = "Loaded",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Loaded",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loadedValue,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Unload Card
            Card(
                modifier = cardModifier,
                colors = cardColors,
                shape = MaterialTheme.shapes.medium // Rounded corners
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_6), // Replace with your image resource
                        contentDescription = "Unload",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Unload",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = unloadValue,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Speed Card (You can replace the image resource)
            Card(
                modifier = cardModifier,
                colors = cardColors,
                shape = MaterialTheme.shapes.medium // Rounded corners
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_8), // Replace with your image resource
                        contentDescription = "Speed",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Speed",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = unloadValue,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Get TKPH Button
        Button(
            onClick = {
                // Fetch TKPH value from the sensorData object
                tkphValue = sensorData.tkph?.toString() ?: "No data"
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp), // Slightly smaller height
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.large // Rounded corners
        ) {
            Text(text = "Get TKPH", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }

        // Display TKPH Value
        Spacer(modifier = Modifier.height(24.dp))
        if (tkphValue != null) {
            Text(
                text = "TKPH Value: $tkphValue",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color(0xFFECEFF1)) // Light gray background
                    .padding(16.dp)
                    .border(2.dp, Color(0xFFB0BEC5), shape = RoundedCornerShape(8.dp)) // Border with rounded corners
            )
        }
    }
}



//Shift HR inside maintenance screen
@Composable
fun ShiftHR(innerPadding: PaddingValues) {
    val scrollState = rememberScrollState()
    var awssValue by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        val cardModifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp)
            .shadow(8.dp, shape = MaterialTheme.shapes.large) // Enhanced shadow

        val cardColors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background // Background color
        )

        // Firebase Database reference
        val database = FirebaseDatabase.getInstance().getReference("sensorData")
        var sensorData by remember { mutableStateOf(SensorData()) }
        var isDataLoaded by remember { mutableStateOf(false) }

        // Fetch data from Firebase
        LaunchedEffect(Unit) {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(SensorData::class.java)
                    if (data != null) {
                        sensorData = data
                        isDataLoaded = true
                    } else {
                        isDataLoaded = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    isDataLoaded = false
                }
            })
        }

        val distanceValue = if (isDataLoaded) sensorData.distance?.toString() ?: "No data" else "No data"
        val rtcValue = if (isDataLoaded) sensorData.rtc?.toString() ?: "No data" else "No data"

        // Display the Distance and RTC values in cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Distance Card
            Card(
                modifier = cardModifier,
                colors = cardColors,
                shape = MaterialTheme.shapes.medium // Rounded corners
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_10), // Replace with your image resource
                        contentDescription = "Distance",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Distance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = distanceValue,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // RTC Card
            Card(
                modifier = cardModifier,
                colors = cardColors,
                shape = MaterialTheme.shapes.medium // Rounded corners
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_9), // Replace with your image resource
                        contentDescription = "RTC",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rtcValue,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Calculate AWSS Button
        Button(
            onClick = {
                // Fetch AWSS value from sensorData and display it
                val calculatedAwss = calculateAwss(sensorData)
                awssValue = calculatedAwss
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp), // Slightly smaller height
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.large // Rounded corners
        ) {
            Text(text = "Calculate AWSS", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        }

        // Display AWSS Value
        Spacer(modifier = Modifier.height(24.dp))
        if (awssValue != null) {
            Text(
                text = "AWSS Value: $awssValue",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color(0xFFECEFF1)) // Light gray background
                    .padding(16.dp)
                    .border(2.dp, Color(0xFFB0BEC5), shape = RoundedCornerShape(8.dp)) // Border with rounded corners
            )
        }
    }
}

// Function to return the AWSS value from sensorData
fun calculateAwss(sensorData: SensorData): String {
    return sensorData.awss?.toString() ?: "No AWSS data available"
}


@Composable
fun CardListScreen(modifier: Modifier = Modifier, tempRef: DatabaseReference, context: Context) {
    var sensorData by remember { mutableStateOf(SensorData()) }
    var showAlert by remember { mutableStateOf(false) }

    DisposableEffect(tempRef) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(SensorData::class.java)
                sensorData = data ?: SensorData()

                // Check if the pressure is below 30% and trigger notification
                val pressure = sensorData.pressure ?: 35f
                if (pressure < 30f) {
                    showAlert = true
                }

                // Check if the temperature is below 0 and trigger notification
                if (sensorData.temperature != null && sensorData.temperature!! < 0) {
                    sendTemperatureAlert(context)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        tempRef.addValueEventListener(listener)
        onDispose {
            tempRef.removeEventListener(listener)
        }
    }

    val temperature = sensorData.temperature ?: 65f
    val pressure = sensorData.pressure ?: 35f
    val ztyrelife = sensorData.ztyrelife?.toIntOrNull() ?: 100
    Log.d("CardListScreen", "Tyre Life: $ztyrelife")

    CardListScreenContent(temperature, pressure, ztyrelife)

    // Show AlertDialog if showAlert is true
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Pressure Alert") },
            text = { Text("The pressure has dropped below 30%. Please check the system.") },
            confirmButton = {
                Button(onClick = { showAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}



@Composable
fun CardListScreenContent(temperature: Float, pressure: Float, ztyrelife: Int) {
    val temperatureColor = getColorForTemperature(temperature)
    val pressureColor = getColorForPressure(pressure)

    // Determine tread life status and color based on the tyrelife value
    val (treadLifeStatus, treadLifeColor) = when {
        ztyrelife <= 40 -> "Alert" to Color.Red
        ztyrelife in 41..70 -> "Normal" to Color.Yellow
        else -> "Good " to Color.Green
    }

    val cardModifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
        .padding(8.dp)
        .shadow(4.dp, shape = MaterialTheme.shapes.medium)

    val cardColors = CardDefaults.cardColors(containerColor = Color.White) // Set card color to white

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 76.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Temperature Section
        Text(
            text = "Temperature",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DonutChartWithLabel(
                    value = temperature,
                    color = temperatureColor,
                    label = "Front Left Tyre"
                )
                DonutChartWithLabel(
                    value = temperature,
                    color = temperatureColor,
                    label = "Front Right Tyre"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DonutChartWithLabel(
                    value = temperature,
                    color = temperatureColor,
                    label = "Back Left Tyre"
                )
                DonutChartWithLabel(
                    value = temperature,
                    color = temperatureColor,
                    label = "Back Right Tyre"
                )
            }
        }

        // Pressure Section
        Text(
            text = "Pressure",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DonutChartWithLabel(
                    value = pressure,
                    color = pressureColor,
                    label = "Front Left Tyre"
                )
                DonutChartWithLabel(
                    value = pressure,
                    color = pressureColor,
                    label = "Front Right Tyre"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DonutChartWithLabel(
                    value = pressure,
                    color = pressureColor,
                    label = "Back Left Tyre"
                )
                DonutChartWithLabel(
                    value = pressure,
                    color = pressureColor,
                    label = "Back Right Tyre"
                )
            }
        }

        // Tyre Life Percentage Card
        Card(
            modifier = cardModifier,
            colors = cardColors
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(180.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tyre Life",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$ztyrelife",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Space between the cards

        // Tread Life Card
        Card(
            modifier = cardModifier,
            colors = cardColors
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Display the tread life status color
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(color = treadLifeColor, shape = CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Tread Depth Condition: $treadLifeStatus",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}



//sidebar
@Composable
fun DrawerContent(drawerState: DrawerState, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the sidebar
                .height(0.5.dp)   // Set the height to 1.dp for a thin line
                .background(Color.Black) // Set the color to black
        )
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = {
                navController.navigate("home")
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the sidebar
                .height(0.5.dp)   // Set the height to 1.dp for a thin line
                .background(Color.Black) // Set the color to black
        )
        NavigationDrawerItem(
            label = { Text("Dashboard") },
            selected = false,
            onClick = {
                navController.navigate("dashboard")
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the sidebar
                .height(0.5.dp)   // Set the height to 1.dp for a thin line
                .background(Color.Black) // Set the color to black
        )
        NavigationDrawerItem(
            label = { Text("Maintenance") },
            selected = false,
            onClick = {
                navController.navigate("maintenance")
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the sidebar
                .height(0.5.dp)   // Set the height to 1.dp for a thin line
                .background(Color.Black) // Set the color to black
        )
        NavigationDrawerItem(
            label = { Text("Map") },
            selected = false,
            onClick = {
                navController.navigate("map")
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the sidebar
                .height(0.5.dp)   // Set the height to 1.dp for a thin line
                .background(Color.Black) // Set the color to black
        )
    }
}

@Composable
fun ChangeSystemBarColor(statusBarColor: Color, navigationBarColor: Color) {
    val systemUiController = rememberSystemUiController()

    systemUiController.setStatusBarColor(
        color = statusBarColor,
        darkIcons = false // Set to true if you want dark icons
    )

    systemUiController.setNavigationBarColor(
        color = navigationBarColor,
        darkIcons = false // Set to true if you want dark icons
    )
}

//home page
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Home(navController: NavController) {
    var isPressed by remember { mutableStateOf(false) }
    // Load Lottie Composition
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.car))

    // Button hover state
    var isHovered by remember { mutableStateOf(false) }
    val buttonColor by animateColorAsState(if (isHovered) Color(0xFF1565C0) else Color(0xFF1976D2))
    val buttonElevation by animateDpAsState(if (isPressed) 8.dp else 4.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFB3E5FC))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Welcome Text
            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Lottie Animation
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .size(300.dp)
                    .border(2.dp, Color(0xFF1976D2), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Get Started Button with Hover Effect
            Button(
                onClick = {
                    navController.navigate("dashboard")
                    // Handle button click
                },
                modifier = Modifier
                    .background(buttonColor)
                    .size(200.dp, 60.dp)
                    .shadow(elevation = buttonElevation, shape = RoundedCornerShape(15.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            }
                        )
                    },
                shape = RoundedCornerShape(15.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(0.dp)
            ) {
                Text("Get Started!", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun Login() {
    // Your login UI implementation here
    Column(
        modifier = Modifier
            .fillMaxSize() // Apply the padding values here
            .padding(16.dp) // Additional padding for content
    ) {
        Text(text = "login here", style = MaterialTheme.typography.headlineLarge)
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TpmsTheme {
        Tpms(tempRef = FirebaseDatabase.getInstance().getReference("sensorData"))
    }
}


fun sendTemperatureAlert(context: Context) {
    val notificationBuilder = NotificationCompat.Builder(context, "TEMP_ALERT_CHANNEL")
        .setSmallIcon(R.drawable.img_8)  // Replace with your notification icon
        .setContentTitle("Temperature Alert")
        .setContentText("The temperature has dropped below 0°C!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        // Notification ID is a unique int for each notification that you must define
        notify(1001, notificationBuilder.build())
    }
}
