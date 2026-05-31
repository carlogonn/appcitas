package com.appcitas.app.ui.screens.discover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.appcitas.app.domain.model.User
import com.appcitas.app.ui.components.VerificationBadge
import com.appcitas.app.ui.theme.ErrorRed
import com.appcitas.app.ui.theme.SuccessGreen
import com.appcitas.app.ui.theme.WarningOrange

@Composable
fun DiscoverScreen(
    onMatchFound: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val users by viewModel.users
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DiscoverEvent.MatchFound -> onMatchFound(event.matchId)
                else -> {}
            }
        }
    }

    if (isLoading && users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No hay más personas cerca",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Intenta ampliar tu rango de búsqueda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.loadUsers() }) {
                    Text("Actualizar")
                }
            }
        }
    } else {
        val currentUser = users.firstOrNull()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // User card
            currentUser?.let { user ->
                SwipeableUserCard(
                    user = user,
                    onSwipeLeft = { viewModel.swipe(user.id, false) },
                    onSwipeRight = { viewModel.swipe(user.id, true) },
                    onSuperLike = { viewModel.swipe(user.id, true) }
                )
            }
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error
        }
    }
}

@Composable
private fun SwipeableUserCard(
    user: User,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSuperLike: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 300),
        label = "offsetX"
    )

    val backgroundColor = when {
        offsetX > 100 -> SuccessGreen.copy(alpha = 0.3f)
        offsetX < -100 -> ErrorRed.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(16.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 150) {
                            onSwipeRight()
                        } else if (offsetX < -150) {
                            onSwipeLeft()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // User photo
            AsyncImage(
                model = user.profilePhotoUrl,
                contentDescription = user.username,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // User info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        VerificationBadge(size = 24.dp)
                    }
                }

                if (user.bio != null) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Cerca de ti",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Swipe indicators
            if (offsetX > 50) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Like",
                        tint = Color.White
                    )
                }
            } else if (offsetX < -50) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(ErrorRed)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dislike",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Action buttons
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = onSwipeLeft,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ErrorRed.copy(alpha = 0.1f))
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "No me gusta",
                tint = ErrorRed,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(
            onClick = onSuperLike,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(WarningOrange.copy(alpha = 0.1f))
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = "Super like",
                tint = WarningOrange,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(
            onClick = onSwipeRight,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SuccessGreen.copy(alpha = 0.1f))
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Me gusta",
                tint = SuccessGreen,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// Need to import Brush
import androidx.compose.ui.graphics.Brush
