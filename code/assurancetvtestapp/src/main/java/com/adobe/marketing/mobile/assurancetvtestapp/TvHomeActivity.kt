package com.adobe.marketing.mobile.assurancetvtestapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.assurance.internal.ui.floatingbutton.AssuranceSessionConnectionIndicator
import com.adobe.marketing.mobile.assurancetvtestapp.ui.theme.AepsdkassuranceandroidTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TvHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        MobileCore.initialize(
            this.application,
            "94f571f308d5/d9220cd8c3aa/launch-2e799e530b10-development"
        ) {
            Log.d("TAG", "MobileCore Initialized")
        }

        setContent {
            AepsdkassuranceandroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape,
                ) {
                    TvNavHost()
                }
            }
        }
    }
}

// Define navigation routes
object AppDestinations {
    const val HOME_ROUTE = "home"
    const val DETAIL_ROUTE = "detail/{contentTitle}"

    fun detailRoute(contentTitle: String): String {
        val encodedTitle = URLEncoder.encode(contentTitle, StandardCharsets.UTF_8.toString())
        return "detail/$encodedTitle"
    }
}

@Composable
fun TvNavHost(navController: NavHostController = rememberNavController()) {

        NavHost(
            navController = navController,
            startDestination = AppDestinations.HOME_ROUTE
        ) {
            composable(AppDestinations.HOME_ROUTE) {
                TvHomeScreen(onCardClick = { contentTitle ->
                    navController.navigate(AppDestinations.detailRoute(contentTitle))
                })
            }

            composable(
                route = AppDestinations.DETAIL_ROUTE,
                arguments = listOf(
                    navArgument("contentTitle") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val contentTitle =
                    backStackEntry.arguments?.getString("contentTitle") ?: "Unknown Content"
                DetailScreen(
                    title = contentTitle,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    AssuranceSessionConnectionIndicator(
            size = 50,
            cornerRadius = 10f,
    )
}

@Composable
fun TvHomeScreen(onCardClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {

        item {
            Text(
                text = "Adobe Mobile TV",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 24.dp, bottom = 20.dp)
            )
        }


        item {
            ContentRow(
                title = "Featured Shows",
                items = listOf("Show 1", "Show 2", "Show 3", "Show 4", "Show 5", "Show 6"),
                onCardClick = onCardClick
            )
        }

        item {
            ContentRow(
                title = "Movies",
                items = listOf("Movie 1", "Movie 2", "Movie 3", "Movie 4", "Movie 5", "Movie 6"),
                onCardClick = onCardClick
            )
        }

        item {
            ContentRow(
                title = "Recently Watched",
                items = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6"),
                onCardClick = onCardClick
            )
        }

        item {
            ContentRow(
                title = "Recommended",
                items = listOf("Rec 1", "Rec 2", "Rec 3", "Rec 4", "Rec 5", "Rec 6"),
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
fun ContentRow(title: String, items: List<String>, onCardClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                ContentCard(title = item, onClick = { onCardClick(item) })
            }
        }
    }
}

@Composable
fun ContentCard(title: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = CardDefaults.shape(RectangleShape),
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        scale = CardDefaults.scale(),
        colors = CardDefaults.colors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun DetailScreen(title: String, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Column(
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "Description",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam euismod, " +
                        "nisl eget aliquam ultricies, nunc nisl aliquet nunc, quis aliquam nisl " +
                        "nunc quis nisl. Nullam euismod, nisl eget aliquam ultricies, nunc nisl " +
                        "aliquet nunc, quis aliquam nisl nunc quis nisl.",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),

            ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Back to Home")
            }
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Some other button")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TvHomeScreenPreview() {
    AepsdkassuranceandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape
        ) {
            TvHomeScreen(onCardClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    AepsdkassuranceandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape
        ) {
            DetailScreen(title = "Show Preview", onBackClick = {})
        }
    }
}