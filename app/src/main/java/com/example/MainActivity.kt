package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        // We obtain an instance of our ViewModel. It will be scoped to the MainActivity lifecycle.
        val viewModel: GpaViewModel = viewModel()
        
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          val navController = rememberNavController()

          NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
          ) {
            composable("home") {
              val currentSemesterResult by viewModel.currentSemesterResult.collectAsState()
              
              HomeScreen(
                currentSemesterResult = currentSemesterResult,
                cgpa = viewModel.cgpa,
                onCalculateSemesterGpaClicked = {
                  navController.navigate("input")
                },
                onSimpleCgpaClicked = {
                  navController.navigate("simple_cgpa_input")
                }
              )
            }

            composable("manage_cgpa") {
              val semesters by viewModel.semesters.collectAsState()
              
              CgpaManageScreen(
                semesters = semesters,
                cgpa = viewModel.cgpa,
                onAddSemesterClicked = {
                  navController.navigate("cgpa_input/new")
                },
                onEditSemesterClicked = { semesterId ->
                  navController.navigate("cgpa_input/$semesterId")
                },
                onDeleteSemesterClicked = { semesterId ->
                  viewModel.deleteSemester(semesterId)
                },
                onBackClicked = {
                  navController.popBackStack("home", inclusive = false)
                }
              )
            }

            // --- Semester GPA Flow ---
            composable("input") {
              CoursesInputScreen(
                onNextClicked = { numberOfCourses ->
                  navController.navigate("course_entry/$numberOfCourses")
                }
              )
            }
            
            composable(
              route = "course_entry/{numberOfCourses}",
              arguments = listOf(navArgument("numberOfCourses") { type = NavType.IntType })
            ) { backStackEntry ->
              val numberOfCourses = backStackEntry.arguments?.getInt("numberOfCourses") ?: 0
              CourseEntryScreen(
                numberOfCourses = numberOfCourses,
                buttonText = "Calculate GPA",
                onCalculateClicked = { courses ->
                    var isValid = true
                    for (course in courses) {
                        if (course.creditHours.isEmpty() || course.grade.isEmpty()) {
                            isValid = false
                            break
                        }
                    }

                    if (isValid) {
                        val result = calculateGpa(courses)
                        viewModel.setCurrentSemesterResult(result)
                        navController.navigate("result/${result.gpa}/${result.totalCredits}/${result.totalGradePoints}")
                    }
                }
              )
            }
            
            composable(
              route = "result/{gpa}/{totalCredits}/{totalGradePoints}",
              arguments = listOf(
                navArgument("gpa") { type = NavType.FloatType },
                navArgument("totalCredits") { type = NavType.FloatType },
                navArgument("totalGradePoints") { type = NavType.FloatType }
              )
            ) { backStackEntry ->
              val gpa = backStackEntry.arguments?.getFloat("gpa") ?: 0f
              val totalCredits = backStackEntry.arguments?.getFloat("totalCredits") ?: 0f
              val totalGradePoints = backStackEntry.arguments?.getFloat("totalGradePoints") ?: 0f
              
              GpaResultScreen(
                gpa = gpa,
                totalCredits = totalCredits,
                totalGradePoints = totalGradePoints,
                onRecalculateClicked = {
                  navController.popBackStack("home", inclusive = false)
                }
              )
            }

            // --- CGPA Flow ---
            composable(
              route = "cgpa_input/{semesterId}",
              arguments = listOf(navArgument("semesterId") { type = NavType.StringType })
            ) { backStackEntry ->
              val semesterId = backStackEntry.arguments?.getString("semesterId") ?: "new"
              
              var initialCountText = ""
              if (semesterId != "new") {
                  val existingSemester = viewModel.getSemester(semesterId)
                  if (existingSemester != null) {
                      initialCountText = existingSemester.courses.size.toString()
                  }
              }

              CoursesInputScreen(
                initialCountText = initialCountText,
                onNextClicked = { numberOfCourses ->
                  navController.navigate("cgpa_course_entry/$numberOfCourses/$semesterId")
                }
              )
            }

            composable(
              route = "cgpa_course_entry/{numberOfCourses}/{semesterId}",
              arguments = listOf(
                navArgument("numberOfCourses") { type = NavType.IntType },
                navArgument("semesterId") { type = NavType.StringType }
              )
            ) { backStackEntry ->
              val numberOfCourses = backStackEntry.arguments?.getInt("numberOfCourses") ?: 0
              val semesterId = backStackEntry.arguments?.getString("semesterId") ?: "new"

              var initialCourses: List<CourseEntry>? = null
              if (semesterId != "new") {
                  initialCourses = viewModel.getSemester(semesterId)?.courses
              }

              CourseEntryScreen(
                numberOfCourses = numberOfCourses,
                initialCourses = initialCourses,
                buttonText = "Save Semester",
                onCalculateClicked = { courses ->
                    var isValid = true
                    for (course in courses) {
                        if (course.creditHours.isEmpty() || course.grade.isEmpty()) {
                            isValid = false
                            break
                        }
                    }

                    if (isValid) {
                        if (semesterId == "new") {
                            val semestersCount = viewModel.semesters.value.size
                            val semester = Semester(
                                name = "Semester ${semestersCount + 1}",
                                courses = courses
                            )
                            viewModel.addSemester(semester)
                        } else {
                            val existingSemester = viewModel.getSemester(semesterId)
                            if (existingSemester != null) {
                                viewModel.updateSemester(existingSemester.copy(courses = courses))
                            }
                        }
                        navController.popBackStack("manage_cgpa", inclusive = false)
                    }
                }
              )
            }
            // --- Simple CGPA Flow ---
            composable("simple_cgpa_input") {
              CoursesInputScreen(
                promptText = "How many semesters have you completed?",
                labelText = "Number of Semesters",
                onNextClicked = { numberOfSemesters ->
                  navController.navigate("simple_cgpa_entry/$numberOfSemesters")
                }
              )
            }

            composable(
              route = "simple_cgpa_entry/{numberOfSemesters}",
              arguments = listOf(navArgument("numberOfSemesters") { type = NavType.IntType })
            ) { backStackEntry ->
              val numberOfSemesters = backStackEntry.arguments?.getInt("numberOfSemesters") ?: 0
              
              SimpleCgpaEntryScreen(
                numberOfSemesters = numberOfSemesters,
                onCalculateClicked = { calculatedCgpa ->
                  navController.navigate("simple_cgpa_result/$calculatedCgpa")
                }
              )
            }
            
            composable(
              route = "simple_cgpa_result/{cgpa}",
              arguments = listOf(navArgument("cgpa") { type = NavType.FloatType })
            ) { backStackEntry ->
              val cgpa = backStackEntry.arguments?.getFloat("cgpa") ?: 0f
              
              SimpleCgpaResultScreen(
                cgpa = cgpa,
                onRecalculateClicked = {
                  navController.popBackStack("home", inclusive = false)
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun CoursesInputScreen(
  modifier: Modifier = Modifier,
  initialCountText: String = "",
  promptText: String = "How many courses are you taking this semester?",
  labelText: String = "Number of Courses",
  onNextClicked: (Int) -> Unit
) {
  // remember saves the state across recompositions.
  // mutableStateOf triggers a recomposition when the value changes.
  var coursesText by remember(initialCountText) { mutableStateOf(initialCountText) }
  var isError by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = promptText,
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(bottom = 24.dp)
    )

    OutlinedTextField(
      value = coursesText,
      onValueChange = { input -> 
        // We only want numbers
        if (input.isEmpty() || input.all { it.isDigit() }) {
          coursesText = input
          isError = false
        }
      },
      label = { Text(labelText) },
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions(
        onDone = {
          val count = coursesText.toIntOrNull() ?: 0
          if (count > 0) {
            onNextClicked(count)
          } else {
            isError = true
          }
        }
      ),
      isError = isError,
      supportingText = if (isError) { { Text("Please enter a valid number greater than 0") } } else null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp)
        .testTag("courses_input")
    )

    Button(
      onClick = {
        val count = coursesText.toIntOrNull() ?: 0
        if (count > 0) {
          onNextClicked(count)
        } else {
          isError = true
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("next_button")
    ) {
      Text("Next")
    }
  }
}

@Preview(showBackground = true)
@Composable
fun CoursesInputPreview() {
  MyApplicationTheme { 
    CoursesInputScreen(onNextClicked = {})
  }
}

fun calculateGpa(courses: List<CourseEntry>): GpaResultData {
    var totalCredits = 0f
    var totalPoints = 0f
    
    for (course in courses) {
        val credits = course.creditHours.toFloatOrNull() ?: 0f
        val gradePoint = when (course.grade) {
            "A" -> 4.0f
            "A-" -> 3.75f
            "B+" -> 3.5f
            "B" -> 3.0f
            "B-" -> 2.75f
            "C+" -> 2.5f
            "C" -> 2.0f
            "D" -> 1.0f
            "F" -> 0.0f
            else -> 0.0f
        }
        
        totalCredits += credits
        totalPoints += credits * gradePoint
    }
    
    val gpa = if (totalCredits > 0) totalPoints / totalCredits else 0f
    return GpaResultData(gpa, totalCredits, totalPoints)
}
