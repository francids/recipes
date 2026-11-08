@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.francids.recipes.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.francids.recipes.data.ConfirmRequest
import com.francids.recipes.data.RecipeDraft
import com.francids.recipes.ui.AppViewModel
import com.francids.recipes.ui.components.ModernTextField
import com.francids.recipes.ui.components.RecipeImage

@Composable
fun AddRecipeScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
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

    BackHandler(onBack = ::closeWithConfirmation)

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.setDraftImage(uri)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(viewModel.tr("add_recipe_screen.title")) },
                navigationIcon = {
                    IconButton(onClick = ::closeWithConfirmation) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentStep > 0) {
                    TextButton(onClick = { currentStep-- }) {
                        Text(viewModel.tr("add_recipe_screen.previous"))
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                if (currentStep < 3) {
                    Button(
                        enabled = isValid,
                        onClick = {
                            if (isValid) {
                                currentStep++
                            } else {
                                viewModel.showMessage(
                                    viewModel.tr("add_recipe_screen.error_complete_fields"),
                                    true,
                                )
                            }
                        },
                    ) {
                        Text(viewModel.tr("add_recipe_screen.next"))
                    }
                } else {
                    Button(
                        enabled = isValid,
                        onClick = {
                            viewModel.saveDraft { saved ->
                                if (saved) onBack()
                            }
                        },
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(viewModel.tr("add_recipe_screen.save"))
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stepTitles[currentStep],
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = viewModel.tr(
                        "add_recipe_screen.step_counter",
                        currentStep + 1,
                        4,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
            ) {
                when (currentStep) {
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
                        type = "ingredients",
                        label = viewModel.tr("add_recipe_screen.ingredient"),
                        items = draft.ingredients,
                        onItemsChange = viewModel::updateDraftIngredients,
                    )

                    else -> DynamicListStep(
                        viewModel = viewModel,
                        type = "directions",
                        label = viewModel.tr("add_recipe_screen.step"),
                        items = draft.directions,
                        onItemsChange = viewModel::updateDraftDirections,
                    )
                }
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
    val draft = viewModel.draft
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onPickImage),
        ) {
            if (draft.imagePath.isBlank()) {
                Card(
                    border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (draft.imagePath.isNotBlank()) {
            Spacer(Modifier.height(24.dp))
            if (viewModel.isLoggedIn) {
                Button(
                    onClick = { viewModel.generateRecipeFromImage { } },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(viewModel.tr("image_step.generate_recipe_info"))
                }
            } else {
                Text(
                    text = viewModel.tr("image_step.feature_not_available"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Composable
private fun DynamicListStep(
    viewModel: AppViewModel,
    type: String,
    label: String,
    items: List<String>,
    onItemsChange: (List<String>) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FilledTonalButton(
            onClick = { onItemsChange(items + "") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(viewModel.tr("dynamic_list.add_item", label))
        }
        Spacer(Modifier.height(16.dp))

        if (items.isEmpty()) {
            Text(
                text = viewModel.tr("dynamic_list.no_items_yet", label.lowercase()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            )
        }

        items.forEachIndexed { index, value ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    IconButton(
                        enabled = index > 0,
                        onClick = {
                            val updated = items.toMutableList()
                            val previous = updated.removeAt(index)
                            updated.add(index - 1, previous)
                            onItemsChange(updated)
                        },
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(
                        enabled = index < items.lastIndex,
                        onClick = {
                            val updated = items.toMutableList()
                            val next = updated.removeAt(index)
                            updated.add(index + 1, next)
                            onItemsChange(updated)
                        },
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = { text ->
                        val updated = items.toMutableList()
                        updated[index] = text
                        onItemsChange(updated)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("$label ${index + 1}") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                IconButton(
                    onClick = {
                        onItemsChange(items.toMutableList().apply { removeAt(index) })
                    },
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
