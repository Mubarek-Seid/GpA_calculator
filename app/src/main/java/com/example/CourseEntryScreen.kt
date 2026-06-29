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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// We need a simple class to hold data for each course being entered.
data class CourseEntry(
    val id: Int,
    var name: String = "",
    var creditHours: String = "",
    var grade: String = ""
)

@Composable
fun CourseEntryScreen(
    modifier: Modifier = Modifier,
    numberOfCourses: Int,
    initialCourses: List<CourseEntry>? = null,
    buttonText: String = "Calculate GPA",
    onCalculateClicked: (List<CourseEntry>) -> Unit
) {
    // We create a list of CourseEntry objects based on the number of courses.
    // We use `remember` so the state survives recompositions.
    val courses = remember(numberOfCourses, initialCourses) {
        val list = mutableListOf<CourseEntry>()
        if (initialCourses != null) {
            list.addAll(initialCourses.map { it.copy() }) // Deep copy
        }
        
        if (list.size > numberOfCourses) {
            // Truncate
            list.subList(numberOfCourses, list.size).clear()
        } else if (list.size < numberOfCourses) {
            // Pad
            val currentSize = list.size
            for (i in 0 until (numberOfCourses - currentSize)) {
                list.add(CourseEntry(id = currentSize + i))
            }
        }
        
        // Reassign IDs to be sequential
        list.forEachIndexed { index, entry -> 
            list[index] = entry.copy(id = index)
        }
        
        list
    }

    // LazyColumn is used for scrollable lists. It's efficient because it only composes
    // the items that are currently visible on the screen.
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(courses.size) { index ->
            CourseItemCard(course = courses[index])
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onCalculateClicked(courses) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calculate_button")
            ) {
                Text(buttonText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseItemCard(course: CourseEntry) {
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
                text = "Course ${course.id + 1}",
                style = MaterialTheme.typography.titleMedium
            )

            // Course Name
            var name by remember { mutableStateOf(course.name) }
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    course.name = it 
                },
                label = { Text("Course Name (Optional)") },
                keyboardOptions = KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Credit Hours
                var creditHours by remember { mutableStateOf(course.creditHours) }
                OutlinedTextField(
                    value = creditHours,
                    onValueChange = { 
                        // Only allow numbers
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            creditHours = it
                            course.creditHours = it
                        }
                    },
                    label = { Text("Credits") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Grade Dropdown
                var expanded by remember { mutableStateOf(false) }
                var selectedGrade by remember { mutableStateOf(course.grade) }
                val grades = listOf("A", "A-", "B+", "B", "B-", "C+", "C", "D", "F")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedGrade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        grades.forEach { gradeSelection ->
                            DropdownMenuItem(
                                text = { Text(gradeSelection) },
                                onClick = {
                                    selectedGrade = gradeSelection
                                    course.grade = gradeSelection
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
