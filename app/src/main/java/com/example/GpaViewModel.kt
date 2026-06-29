package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class Semester(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val courses: List<CourseEntry>
) {
    val totalCredits: Float
        get() = courses.sumOf { (it.creditHours.toFloatOrNull() ?: 0f).toDouble() }.toFloat()

    val totalGradePoints: Float
        get() = courses.sumOf { 
            val credits = it.creditHours.toFloatOrNull() ?: 0f
            val gradePoint = when (it.grade) {
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
            (credits * gradePoint).toDouble()
        }.toFloat()

    val gpa: Float
        get() = if (totalCredits > 0) totalGradePoints / totalCredits else 0f
}

data class GpaResultData(val gpa: Float, val totalCredits: Float, val totalGradePoints: Float)

class GpaViewModel : ViewModel() {
    private val _semesters = MutableStateFlow<List<Semester>>(emptyList())
    val semesters: StateFlow<List<Semester>> = _semesters.asStateFlow()

    private val _currentSemesterResult = MutableStateFlow<GpaResultData?>(null)
    val currentSemesterResult: StateFlow<GpaResultData?> = _currentSemesterResult.asStateFlow()

    val cgpa: Float
        get() {
            val sems = _semesters.value
            val totalCredits = sems.sumOf { it.totalCredits.toDouble() }.toFloat()
            val totalPoints = sems.sumOf { it.totalGradePoints.toDouble() }.toFloat()
            return if (totalCredits > 0) totalPoints / totalCredits else 0f
        }

    fun setCurrentSemesterResult(result: GpaResultData) {
        _currentSemesterResult.value = result
    }

    fun addSemester(semester: Semester) {
        _semesters.update { currentList -> 
            currentList + semester 
        }
    }

    fun updateSemester(updatedSemester: Semester) {
        _semesters.update { currentList ->
            currentList.map { if (it.id == updatedSemester.id) updatedSemester else it }
        }
    }

    fun getSemester(id: String): Semester? {
        return _semesters.value.find { it.id == id }
    }

    fun deleteSemester(id: String) {
        _semesters.update { currentList -> 
            currentList.filter { it.id != id } 
        }
    }
}
