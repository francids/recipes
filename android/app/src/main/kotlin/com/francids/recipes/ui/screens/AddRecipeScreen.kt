@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.francids.recipes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.data.RecipeDraft
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.BackTopBar
import com.francids.recipes.ui.components.ModernTextField
import com.francids.recipes.ui.components.PrimaryButton
import com.francids.recipes.ui.components.RecipeImage
import com.francids.recipes.ui.components.SecondaryButton
import com.francids.recipes.ui.theme.AppTheme
import com.francids.recipes.ui.theme.DeepOrange

@Composable
fun AddRecipeScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val palette = AppTheme.palette
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val stepTitles = listOf(
        viewModel.tr("add_recipe_screen.step_image"),
        viewModel.tr("add_recipe_screen.step_details"),
        viewModel.tr("add_recipe_screen.step_ingredients"),
        viewModel.tr("add_recipe_screen.step_instructions"),
    )
    val draft = viewModel.draft
    val isValid = isValidStep(currentStep, draft)

    fun closeWithConfirmation() {
        if (draft.hasData) {
            viewModel.showConfirmation(
                ConfirmRequest(
                    title = viewModel.tr("add_recipe_screen.exit_title"),
                    message = viewModel.tr("add_recipe_screen.exit_without_saving"),
                    confirmLabel = viewModel.tr("ui_helpers.confirm"),
                    cancelLabel = viewModel.tr("ui_helpers.cancel"),
                    destructive = true,
                    onConfirm = {
                        viewModel.resetDraft()
                        onBack()
                    },
                ),
            )
        } else {
            viewModel.resetDraft()
            onBack()
        }
    }

    BackHandler { closeWithConfirmation() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.setDraftImage(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        BackTopBar(
            title = viewModel.tr("add_recipe_screen.title"),
            onBack = { closeWithConfirmation() },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(
                text = stepTitles[currentStep],
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = palette.textPrimary,
            )
            Text(
                text = viewModel.tr(
                    "add_recipe_screen.step_counter",
                    currentStep + 1,
                    4,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textTertiary,
            )
        }

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(100)) },
            label = "recipe-step",
            modifier = Modifier.weight(1f),
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 80.dp),
            ) {
                when (step) {
                    0 -> ImageStep(
                        viewModel = viewModel,
                        onPickImage = {
                            imagePicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                    )

                    1 -> DetailsStep(viewModel)
                    2 -> DynamicListStep(
                        viewModel = viewModel,
                        label = viewModel.tr("add_recipe_screen.ingredient"),
                        items = draft.ingredients,
                        onItemsChange = viewModel::updateDraftIngredients,
                    )

                    else -> DynamicListStep(
                        viewModel = viewModel,
                        label = viewModel.tr("add_recipe_screen.step"),
                        items = draft.directions,
                        onItemsChange = viewModel::updateDraftDirections,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (currentStep > 0) {
                SecondaryButton(onClick = { currentStep-- }) {
                    Text(viewModel.tr("add_recipe_screen.previous"))
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            if (currentStep < 3 && isValid) {
                PrimaryButton(onClick = { currentStep++ }) {
                    Text(viewModel.tr("add_recipe_screen.next"))
                }
            } else if (currentStep == 3 && isValid) {
                PrimaryButton(
                    onClick = {
                        viewModel.saveDraft { saved ->
                            if (saved) onBack()
                        }
                    },
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(viewModel.tr("add_recipe_screen.save"))
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }
        }
    }
}

private fun isValidStep(step: Int, draft: RecipeDraft): Boolean = when (step) {
    0 -> draft.imagePath.isNotBlank()
    1 -> draft.title.isNotBlank() && draft.description.isNotBlank() &&
        draft.preparationTime > 0

    2 -> draft.ingredients.isNotEmpty() && draft.ingredients.all { it.isNotBlank() }
    3 -> draft.directions.isNotEmpty() && draft.directions.all { it.isNotBlank() }
    else -> true
}

@Composable
private fun ImageStep(
    viewModel: AppViewModel,
    onPickImage: () -> Unit,
) {
    val palette = AppTheme.palette
    val draft = viewModel.draft
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.surface)
                .border(3.dp, DeepOrange, RoundedCornerShape(10.dp))
                .clickable(onClick = onPickImage),
            contentAlignment = Alignment.Center,
        ) {
            if (draft.imagePath.isBlank()) {
                Icon(
                    Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = DeepOrange,
                    modifier = Modifier.size(40.dp),
                )
            } else {
                RecipeImage(
                    path = draft.imagePath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = viewModel.tr("image_step.tap_to_select"),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textTertiary,
            textAlign = TextAlign.Center,
        )

        if (draft.imagePath.isNotBlank()) {
            Spacer(Modifier.height(24.dp))
            if (viewModel.isLoggedIn) {
                PrimaryButton(
                    onClick = { viewModel.generateRecipeFromImage { } },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = viewModel.tr("image_step.generate_recipe_info"),
                        color = Color.White,
                    )
                }
            } else {
                Text(
                    text = viewModel.tr("image_step.feature_not_available"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textTertiary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun DetailsStep(viewModel: AppViewModel) {
    val draft = viewModel.draft
    Column(modifier = Modifier.fillMaxWidth()) {
        ModernTextField(
            value = draft.title,
            onValueChange = viewModel::updateDraftTitle,
            label = viewModel.tr("text_fields_step.title_label"),
            hint = viewModel.tr("text_fields_step.title_hint"),
        )
        ModernTextField(
            value = draft.description,
            onValueChange = viewModel::updateDraftDescription,
            label = viewModel.tr("text_fields_step.description_label"),
            hint = viewModel.tr("text_fields_step.description_hint"),
            singleLine = false,
            minLines = 3,
        )
        val minutes = if (draft.preparationTime > 0) {
            (draft.preparationTime / 60).toString()
        } else {
            ""
        }
        ModernTextField(
            value = minutes,
            onValueChange = { text ->
                val parsed = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                viewModel.updateDraftPreparationMinutes(parsed)
            },
            label = viewModel.tr("text_fields_step.preparation_time_label"),
            hint = viewModel.tr("text_fields_step.preparation_time_hint"),
            keyboardType = KeyboardType.Number,
            suffix = {
                Text(
                    viewModel.tr("text_fields_step.minutes"),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.palette.textTertiary,
                )
            },
        )
    }
}

@Composable
private fun DynamicListStep(
    viewModel: AppViewModel,
    label: String,
    items: List<String>,
    onItemsChange: (List<String>) -> Unit,
) {
    val palette = AppTheme.palette
    Column(modifier = Modifier.fillMaxWidth()) {
        PrimaryButton(
            onClick = { onItemsChange(items + "") },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(viewModel.tr("dynamic_list.add_item", label))
        }
        Spacer(Modifier.height(16.dp))

        if (items.isEmpty()) {
            Text(
                text = viewModel.tr("dynamic_list.no_items_yet", label.lowercase()),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                ),
                color = palette.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            )
        }

        items.forEachIndexed { index, value ->
            var dragOffset by remember(items.size) { mutableFloatStateOf(0f) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = null,
                    tint = palette.textTertiary,
                    modifier = Modifier
                        .size(24.dp)
                        .pointerInput(index, items) {
                            detectDragGesturesAfterLongPress(
                                onDrag = { change, amount ->
                                    change.consume()
                                    dragOffset += amount.y
                                },
                                onDragEnd = {
                                    val delta = (dragOffset / 72f).roundToInt()
                                    val target = (index + delta).coerceIn(0, items.lastIndex)
                                    if (target != index) {
                                        val updated = items.toMutableList()
                                        val moved = updated.removeAt(index)
                                        updated.add(target, moved)
                                        onItemsChange(updated)
                                    }
                                    dragOffset = 0f
                                },
                                onDragCancel = { dragOffset = 0f },
                            )
                        },
                )
                Spacer(Modifier.width(8.dp))
                ModernTextField(
                    value = value,
                    onValueChange = { text ->
                        val updated = items.toMutableList()
                        updated[index] = text
                        onItemsChange(updated)
                    },
                    hint = "$label ${index + 1}",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        onItemsChange(items.toMutableList().apply { removeAt(index) })
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.RemoveCircle,
                        contentDescription = null,
                        tint = Color.Red,
                    )
                }
            }
        }
    }
}
