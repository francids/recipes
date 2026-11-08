package com.francids.recipes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.francids.recipes.ui.components.AppCard
import com.francids.recipes.ui.components.AppDivider
import com.francids.recipes.ui.components.BackTopBar
import com.francids.recipes.ui.components.EmptyState
import com.francids.recipes.ui.components.RecipeImage
import com.francids.recipes.ui.components.SectionHeading
import com.francids.recipes.ui.theme.AppTheme
import com.francids.recipes.ui.theme.DeepOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ProfileInfoScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val palette = AppTheme.palette
    val user = viewModel.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        BackTopBar(
            title = viewModel.tr("profile_info_screen.title"),
            onBack = onBack,
        )

        if (user == null) {
            EmptyState(
                text = viewModel.tr("profile_info_screen.not_signed_in"),
                modifier = Modifier.weight(1f),
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ProfileAvatar(user) }

            item {
                SectionHeading(viewModel.tr("profile_info_screen.personal_info"))
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        InfoRow(
                            title = viewModel.tr("profile_info_screen.display_name"),
                            value = user.name.ifBlank {
                                viewModel.tr("profile_info_screen.not_available")
                            },
                            icon = Icons.Outlined.Person,
                        )
                        AppDivider()
                        InfoRow(
                            title = viewModel.tr("profile_info_screen.email"),
                            value = user.email.ifBlank {
                                viewModel.tr("profile_info_screen.not_available")
                            },
                            icon = Icons.Outlined.Email,
                        )
                        AppDivider()
                        InfoRow(
                            title = viewModel.tr("profile_info_screen.user_id"),
                            value = user.id,
                            icon = Icons.Outlined.Fingerprint,
                        )
                    }
                }
            }

            item {
                SectionHeading(viewModel.tr("profile_info_screen.account_details"))
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
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
                                Color(0xFF4CAF50)
                            } else {
                                Color(0xFFFF9800)
                            },
                        )
                        AppDivider()
                        InfoRow(
                            title = viewModel.tr("profile_info_screen.account_created"),
                            value = formatRelativeDate(user.registration, viewModel),
                            icon = Icons.Outlined.CalendarMonth,
                        )
                        AppDivider()
                        InfoRow(
                            title = viewModel.tr("profile_info_screen.last_sign_in"),
                            value = formatRelativeDate(user.accessedAt, viewModel),
                            icon = Icons.Outlined.Schedule,
                        )
                    }
                }
            }

            item {
                Text(
                    text = viewModel.tr("profile_info_screen.danger_zone"),
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.Red,
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.Red.copy(alpha = 0.10f))
                        .border(
                            width = 0.5.dp,
                            color = Color.Red.copy(alpha = 0.30f),
                            shape = MaterialTheme.shapes.medium,
                        )
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
                                    animationAsset = "lottie/delete.json",
                                    onConfirm = {
                                        viewModel.deleteAccount(onFinished = onBack)
                                    },
                                ),
                            )
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.tr("profile_info_screen.delete_account"),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = Color.Red,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = viewModel.tr("profile_info_screen.delete_account_warning"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red.copy(alpha = 0.70f),
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Red,
                        )
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
        Box(
            modifier = Modifier
                .size(126.dp)
                .clip(CircleShape)
                .border(2.dp, AppTheme.palette.border, CircleShape)
                .background(DeepOrange.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            if (user.picture.isNullOrBlank()) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = DeepOrange,
                    modifier = Modifier.size(60.dp),
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
private fun InfoRow(
    title: String,
    value: String,
    icon: ImageVector,
    valueColor: Color? = null,
) {
    val palette = AppTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DeepOrange,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = valueColor ?: palette.textTertiary,
            )
        }
    }
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
