@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.francids.recipes.data.AppUser
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.EmptyState
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
                    .padding(padding),
            ) {
                EmptyState(
                    title = viewModel.tr("profile_info_screen.not_signed_in"),
                    message = viewModel.tr("profile_info_screen.sign_in_to_view"),
                    icon = Icons.Outlined.Error,
                )
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
            item { ProfileAvatar(user) }

            item {
                SectionLabel(viewModel.tr("profile_info_screen.personal_info"))
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
                SectionLabel(viewModel.tr("profile_info_screen.account_details"))
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
                        iconContainerColor = if (user.emailVerification) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                        iconContentColor = if (user.emailVerification) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                    )
                    CardDivider()
                    InfoRow(
                        title = viewModel.tr("profile_info_screen.account_created"),
                        value = formatRelativeDate(user.registration, viewModel),
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
                SectionLabel(
                    text = viewModel.tr("profile_info_screen.danger_zone"),
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
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                viewModel.tr("profile_info_screen.delete_account"),
                                style = MaterialTheme.typography.titleMediumEmphasized,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                viewModel.tr("profile_info_screen.delete_account_warning"),
                                style = MaterialTheme.typography.bodySmall,
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
private fun ProfileAvatar(user: AppUser) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(136.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (user.picture.isNullOrBlank()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
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
}

@Composable
private fun SectionLabel(text: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLargeEmphasized,
        color = color,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column { content() }
    }
}

@Composable
private fun InfoRow(
    title: String,
    value: String,
    icon: ImageVector,
    iconContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    iconContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = iconContainerColor,
            contentColor = iconContentColor,
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
