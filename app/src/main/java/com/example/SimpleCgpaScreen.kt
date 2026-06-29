package com.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

data class SimpleSemesterEntry(
    val id: Int,
    var gpa: String = ""
)

@Composable
fun SimpleCgpaEntryScreen(
    numberOfSemesters: Int,
    onCalculateClicked: (Float) -> Unit
) {
    val semesters = remember(numberOfSemesters) {
        List(numberOfSemesters) { index -> SimpleSemesterEntry(id = index) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(semesters.size) { index ->
            SimpleSemesterCard(semester = semesters[index])
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    var totalGpa = 0f
                    var validCount = 0
                    for (semester in semesters) {
                        val gpaValue = semester.gpa.toFloatOrNull()
                        if (gpaValue != null) {
                            totalGpa += gpaValue
                            validCount++
                        }
                    }
                    val cgpa = if (validCount > 0) totalGpa / validCount else 0f
                    onCalculateClicked(cgpa)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate CGPA")
            }
        }
    }
}

@Composable
fun SimpleSemesterCard(semester: SimpleSemesterEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Semester ${semester.id + 1}",
                style = MaterialTheme.typography.titleMedium
            )

            var gpa by remember { mutableStateOf(semester.gpa) }
            OutlinedTextField(
                value = gpa,
                onValueChange = {
                    // allow numbers and decimal point
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        gpa = it
                        semester.gpa = it
                    }
                },
                label = { Text("Semester GPA") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SimpleCgpaResultScreen(
    cgpa: Float,
    onRecalculateClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Overall CGPA",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = String.format(Locale.US, "%.2f", cgpa),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onRecalculateClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate Another CGPA")
        }
    }
}
