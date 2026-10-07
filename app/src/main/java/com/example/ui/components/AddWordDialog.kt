package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddWordDialog(
    onDismiss: () -> Unit,
    onSave: (
        english: String,
        urdu: String,
        romanUrdu: String,
        partOfSpeech: String,
        definition: String,
        urduDefinition: String,
        exampleEn: String,
        exampleUr: String,
        category: String
    ) -> Unit
) {
    var english by remember { mutableStateOf("") }
    var urdu by remember { mutableStateOf("") }
    var romanUrdu by remember { mutableStateOf("") }
    var partOfSpeech by remember { mutableStateOf("Noun (اسم)") }
    var definition by remember { mutableStateOf("") }
    var urduDefinition by remember { mutableStateOf("") }
    var exampleEn by remember { mutableStateOf("") }
    var exampleUr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("تعلیم (Academic)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "نیا لفظ شامل کریں (Add Custom Word)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = english,
                    onValueChange = { english = it },
                    label = { Text("انگریزی لفظ (English Word)*") },
                    modifier = Modifier.fillMaxWidth().testTag("input_custom_english"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = urdu,
                    onValueChange = { urdu = it },
                    label = { Text("اردو معنی (Urdu Translation)*") },
                    modifier = Modifier.fillMaxWidth().testTag("input_custom_urdu"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = romanUrdu,
                    onValueChange = { romanUrdu = it },
                    label = { Text("رومن اردو (Roman Urdu)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_custom_roman"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = partOfSpeech,
                    onValueChange = { partOfSpeech = it },
                    label = { Text("قسمِ کلام (Part of Speech e.g. Noun, Verb)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("شعبہ (Category e.g. Medical, Science, Law)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = definition,
                    onValueChange = { definition = it },
                    label = { Text("English Definition") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = urduDefinition,
                    onValueChange = { urduDefinition = it },
                    label = { Text("اردو میں تشریح (Urdu Explanation)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = exampleEn,
                    onValueChange = { exampleEn = it },
                    label = { Text("English Example") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = exampleUr,
                    onValueChange = { exampleUr = it },
                    label = { Text("اردو مثال (Urdu Example)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (english.isNotBlank() && urdu.isNotBlank()) {
                        onSave(
                            english, urdu, romanUrdu, partOfSpeech,
                            definition, urduDefinition, exampleEn, exampleUr, category
                        )
                    }
                },
                enabled = english.isNotBlank() && urdu.isNotBlank(),
                modifier = Modifier.testTag("submit_custom_word_btn")
            ) {
                Text("محفوظ کریں (Save)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ کریں (Cancel)")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
