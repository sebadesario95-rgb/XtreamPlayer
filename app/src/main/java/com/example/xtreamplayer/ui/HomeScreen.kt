package com.example.xtreamplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xtreamplayer.viewmodel.AppViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: AppViewModel,
    onPlay: (String) -> Unit
) {
    val account = vm.auth?.user_info

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "XTREAM PLAYER",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {

                    TextButton(
                        onClick = {
                            vm.updateCatalog()
                        },
                        enabled = !vm.loading
                    ) {
                        Text(
                            "UPDATE",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = {
                            vm.logout()
                        }
                    ) {
                        Text("LOGOUT")
                    }
                }
            )
        }
    ) { pad ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(Color(0xFF090909))
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {

                Spacer(
                    modifier = Modifier.height(35.dp)
                )

                Text(
                    text = "Benvenuto ${account?.username ?: ""}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(45.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        ),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    HomeCategoryCard(
                        title = "LIVE TV",
                        subtitle = "Canali televisivi",
                        onClick = {
                            // Verrà collegato alla schermata LIVE
                        }
                    )

                    HomeCategoryCard(
                        title = "FILM",
                        subtitle = "Film e cinema",
                        onClick = {
                            // Verrà collegato alla schermata FILM
                        }
                    )

                    HomeCategoryCard(
                        title = "SERIE TV",
                        subtitle = "Serie e stagioni",
                        onClick = {
                            // Verrà collegato alla schermata SERIE
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                account?.exp_date?.let { expiration ->

                    Text(
                        text = "SCADENZA ABBONAMENTO",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = formatExpirationDate(expiration),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            if (vm.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            "Aggiornamento catalogo..."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCategoryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(190.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF151515)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = subtitle,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatExpirationDate(
    expiration: String
): String {

    return try {

        val timestamp = expiration.toLong()

        val date = java.text.SimpleDateFormat(
            "dd/MM/yyyy",
            java.util.Locale.getDefault()
        )

        date.format(
            java.util.Date(timestamp * 1000)
        )

    } catch (e: Exception) {

        expiration
    }
}
