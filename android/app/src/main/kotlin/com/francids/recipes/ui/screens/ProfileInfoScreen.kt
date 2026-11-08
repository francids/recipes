package com.francids.recipes.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.francids.recipes.data.AppUser
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.RecipeImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ProfileInfoScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val user = viewModel.user

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackTopBar(
                title = viewModel.tr("profile_info_screen.title"),
                onBack = onBack,
            )
        },
    ) { padding ->
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Error,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        viewModel.tr("profile_info_screen.not_signed_in"),
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        viewModel.tr("profile_info_screen.sign_in_to_view"),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ProfileAvatar(user, viewModel) }

            item {
                Text(
                    viewModel.tr("profile_info_screen.personal_info"),
                    style = MaterialTheme.typography.displayMedium,
                )
            }
            item {
                InfoCard {
                    InfoRow(
                        title = viewModel.tr("profile_info_screen.display_name"),
                        value = user.name.ifBlank {
                            viewModel.tr("profile_info_screen.not_available")
                        },
                        icon = Icons.Outlined.Person,
                    )
                    CardDivider()
                    InfoRow(
                        title = viewModel.tr("profile_info_screen.email"),
                        value = user.email.ifBlank {
                            viewModel.tr("profile_info_screen.not_available")
                        },
                        icon = Icons.Outlined.Email,
                    )
                    CardDivider()
                    InfoRow(
                        title = viewModel.tr("profile_info_screen.user_id"),
                        value = user.id,
                        icon = Icons.Outlined.Fingerprint,
                    )
                }
            }

            item {
                Text(
                    viewModel.tr("profile_info_screen.account_details"),
                    style = MaterialTheme.typography.displayMedium,
                )
            }
            item {
                InfoCard {
                    InfoRow(
                        title = viewModel.tr("profile_info_screen.email_verified"),
                        value = if (user.emailVerification) {
                            viewModel.tr("profile_info_screen.verified")
                        } else {
                            viewModel.tr("profile_info_screen.not_verified")
                        },
                        icon = if (user.emailVerification) {
                            Icons.Outlined.CheckCircle
                        } else {
                            Icons.Outlined.Error
                        },
                        valueColor = if (user.emailVerification) {
                            Color(0xFF2E7D32)
                        } else {
                            Color(0xFFEF6C00)
                        },
                    )
                    CardDivider()
                    InfoRow(
                        title = viewModel.tr("profile_info_screen.account_created"),
                        value = formatRelativeDate(
                            user.registration,
                            viewModel,
                        ),
                        icon = Icons.Outlined.CalendarMonth,
                    )
                    CardDivider()
                    InfoRow(
                        title = viewModel.tr("profile_info_screen.last_sign_in"),
                        value = formatRelativeDate(user.accessedAt, viewModel),
                        icon = Icons.Outlined.Schedule,
                    )
                }
            }

            item {
                Text(
                    viewModel.tr("profile_info_screen.danger_zone"),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.showConfirmation(
                                ConfirmRequest(
                                    title = viewModel.tr("profile_info_screen.confirm_delete_title"),
                                    message = viewModel.tr(
                                        "profile_info_screen.confirm_delete_enhanced_message_with_reauth",
                                    ),
                                    confirmLabel = viewModel.tr("profile_info_screen.delete"),
                                    cancelLabel = viewModel.tr("profile_info_screen.cancel"),
                                    destructive = true,
                                    onConfirm = {
                                        viewModel.deleteAccount(onFinished = onBack)
                                    },
                                ),
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                viewModel.tr("profile_info_screen.delete_account"),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                viewModel.tr("profile_info_screen.delete_account_warning"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ProfileAvatar(user: AppUser, viewModel: AppViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (user.picture.isNullOrBlank()) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                RecipeImage(
                    path = user.picture,
                    contentDescription = user.name,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun InfoRow(
    title: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, color = valueColor)
        }
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

private fun formatRelativeDate(raw: String, viewModel: AppViewModel): String {
    if (raw.isBlank()) return viewModel.tr("profile_info_screen.not_available")
    val date = parseDate(raw) ?: return viewModel.tr("profile_info_screen.not_available")
    val difference = System.currentTimeMillis() - date.time
    val days = difference / (1000 * 60 * 60 * 24)
    val hours = difference / (1000 * 60 * 60)
    val minutes = difference / (1000 * 60)

    return when {
        days > 365 -> viewModel.tr("profile_info_screen.years_ago", days / 365)
        days > 30 -> viewModel.tr("profile_info_screen.months_ago", days / 30)
        days > 0 -> viewModel.tr("profile_info_screen.days_ago", days)
        hours > 0 -> viewModel.tr("profile_info_screen.hours_ago", hours)
        else -> viewModel.tr("profile_info_screen.minutes_ago", minutes)
    }
}

private fun parseDate(raw: String): Date? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
    )
    patterns.forEach { pattern ->
        runCatching {
            val format = SimpleDateFormat(pattern, Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            return format.parse(raw)
        }
    }
    return null
}
