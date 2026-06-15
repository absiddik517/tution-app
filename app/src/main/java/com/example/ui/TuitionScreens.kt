package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuitionMainScreen(viewModel: TuitionViewModel) {
    val activeSection by viewModel.activeSection.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()
    val progressLogs by viewModel.allProgressLogs.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val attendanceForDate by viewModel.attendanceForSelectedDate.collectAsStateWithLifecycle()

    val schedules by viewModel.allSchedules.collectAsStateWithLifecycle()
    val examResults by viewModel.allExamResults.collectAsStateWithLifecycle()
    val sessionLogs by viewModel.allSessionLogs.collectAsStateWithLifecycle()
    val activeTimers by viewModel.activeTimers.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeSection == Section.DASHBOARD,
                    onClick = { viewModel.selectSection(Section.DASHBOARD) },
                    icon = { Icon(Icons.Rounded.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = activeSection == Section.STUDENTS,
                    onClick = { viewModel.selectSection(Section.STUDENTS) },
                    icon = { Icon(Icons.Rounded.People, contentDescription = "Students") },
                    label = { Text("Students", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = activeSection == Section.SCHEDULE,
                    onClick = { viewModel.selectSection(Section.SCHEDULE) },
                    icon = { Icon(Icons.Rounded.Schedule, contentDescription = "Schedule") },
                    label = { Text("Schedule", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = activeSection == Section.ATTENDANCE,
                    onClick = { viewModel.selectSection(Section.ATTENDANCE) },
                    icon = { Icon(Icons.Rounded.EventAvailable, contentDescription = "Attendance") },
                    label = { Text("Attendance", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = activeSection == Section.FEES,
                    onClick = { viewModel.selectSection(Section.FEES) },
                    icon = { Icon(Icons.Rounded.Payments, contentDescription = "Fees") },
                    label = { Text("Fees", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
                NavigationBarItem(
                    selected = activeSection == Section.PROGRESS,
                    onClick = { viewModel.selectSection(Section.PROGRESS) },
                    icon = { Icon(Icons.Rounded.Timeline, contentDescription = "Progress") },
                    label = { Text("Academic", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Main screen routing
                Crossfade(targetState = activeSection, label = "ScreenTransition") { section ->
                    when (section) {
                        Section.DASHBOARD -> DashboardScreen(viewModel, students, payments, progressLogs, attendanceForDate)
                        Section.STUDENTS -> StudentsScreen(viewModel, students)
                        Section.SCHEDULE -> ScheduleScreen(viewModel, students, schedules, sessionLogs, activeTimers)
                        Section.ATTENDANCE -> AttendanceScreen(viewModel, students, sessionLogs, selectedDate)
                        Section.FEES -> FeesScreen(viewModel, students, payments)
                        Section.PROGRESS -> ProgressScreen(viewModel, students, progressLogs, examResults)
                    }
                }

                // Global overlay sheets / dialogs
                viewModel.selectedStudentForDetail?.let { student ->
                    StudentDetailOverlay(
                        student = student,
                        viewModel = viewModel,
                        onDismiss = { viewModel.selectedStudentForDetail = null }
                    )
                }

                // Add Student Dialog
                if (viewModel.showAddStudentDialog) {
                    AddEditStudentDialog(
                        onDismiss = { viewModel.showAddStudentDialog = false },
                        onSave = { name, grade, subject, fee, phone, notes, cycle ->
                            viewModel.saveStudent(
                                name = name,
                                grade = grade,
                                subject = subject,
                                monthlyFee = fee,
                                contactNumber = phone,
                                notes = notes,
                                paymentCycleDays = cycle
                            )
                            viewModel.showAddStudentDialog = false
                            Toast.makeText(context, "Student added successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Edit Student Dialog
                viewModel.studentToEdit?.let { student ->
                    AddEditStudentDialog(
                        student = student,
                        onDismiss = { viewModel.studentToEdit = null },
                        onSave = { name, grade, subject, fee, phone, notes, cycle ->
                            viewModel.saveStudent(
                                id = student.id,
                                name = name,
                                grade = grade,
                                subject = subject,
                                monthlyFee = fee,
                                contactNumber = phone,
                                notes = notes,
                                isActive = student.isActive,
                                paymentCycleDays = cycle
                            )
                            viewModel.studentToEdit = null
                            Toast.makeText(context, "Student details updated!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Quick add payment dialog
                viewModel.showAddPaymentDialogForStudent?.let { student ->
                    AddPaymentDialog(
                        student = student,
                        viewModel = viewModel,
                        onDismiss = { viewModel.showAddPaymentDialogForStudent = null },
                        onSave = { amount, month, notes ->
                            viewModel.logFeePayment(
                                studentId = student.id,
                                amount = amount,
                                monthCovered = month,
                                notes = notes
                            )
                            viewModel.showAddPaymentDialogForStudent = null
                            Toast.makeText(context, "Logged payment of $$amount for ${student.name}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Quick add progress dialog
                viewModel.showAddProgressDialogForStudent?.let { student ->
                    AddProgressDialog(
                        student = student,
                        onDismiss = { viewModel.showAddProgressDialogForStudent = null },
                        onSave = { topic, rating, remarks ->
                            viewModel.logProgress(
                                studentId = student.id,
                                topic = topic,
                                rating = rating,
                                remarks = remarks
                            )
                            viewModel.showAddProgressDialogForStudent = null
                            Toast.makeText(context, "Progress logged for ${student.name}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Quick add schedule dialog
                viewModel.showAddScheduleDialogForStudent?.let { student ->
                    AddScheduleDialog(
                        student = student,
                        onDismiss = { viewModel.showAddScheduleDialogForStudent = null },
                        onSave = { day, time, duration, notes ->
                            viewModel.saveSchedule(
                                studentId = student.id,
                                dayOfWeek = day,
                                timeString = time,
                                durationMinutes = duration,
                                notes = notes
                            )
                            viewModel.showAddScheduleDialogForStudent = null
                            Toast.makeText(context, "Added weekly schedule for ${student.name} on ${day}s!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Quick add exam result dialog
                viewModel.showAddExamDialogForStudent?.let { student ->
                    AddExamDialog(
                        student = student,
                        onDismiss = { viewModel.showAddExamDialogForStudent = null },
                        onSave = { subject, examName, total, obtained, remarks, date ->
                            viewModel.logExamResult(
                                studentId = student.id,
                                subject = subject,
                                examName = examName,
                                totalMarks = total,
                                obtainedMarks = obtained,
                                remarks = remarks,
                                date = date
                            )
                            viewModel.showAddExamDialogForStudent = null
                            Toast.makeText(context, "Exam results logged for ${student.name}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: TuitionViewModel,
    students: List<Student>,
    payments: List<FeePayment>,
    progressLogs: List<ProgressLog>,
    attendanceForDate: List<Attendance>
) {
    val activeStudents = students.filter { it.isActive }
    val currentMonth = viewModel.monthFormat.format(Date())

    // Metrics calculations
    val todayPresent = attendanceForDate.count { it.status == "PRESENT" || it.status == "LATE" }
    val todayLogged = attendanceForDate.size
    val attendanceRate = if (todayLogged > 0) {
        (todayPresent.toFloat() / todayLogged * 100).toInt()
    } else 0

    val currentMonthPayments = payments.filter { it.monthCovered == currentMonth }
    val collectedTotal = currentMonthPayments.sumOf { it.amount }
    
    val paidStudentIds = currentMonthPayments.map { it.studentId }.toSet()
    val pendingCount = activeStudents.count { it.id !in paidStudentIds }
    
    val avgRating = if (progressLogs.isNotEmpty()) {
        progressLogs.map { it.rating }.average()
    } else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Welcome Header
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Tuition Tracker",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Manage attendance, fees, and progress effortlessly.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        // Action Quick Grids
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Quick Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You have $pendingCount student payments pending for $currentMonth.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showAddStudentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.PersonAdd, contentDescription = "Add Student", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Student", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = { viewModel.selectSection(Section.ATTENDANCE) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f), contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Event, contentDescription = "Daily Register", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Daily Register", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Dynamic Statistics Card Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Performance Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Active Students",
                        value = "${activeStudents.size}",
                        icon = Icons.Rounded.People,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Attendance Rate",
                        value = if (todayLogged > 0) "$attendanceRate%" else "--",
                        subtitle = "For today",
                        icon = Icons.Rounded.CheckCircleOutline,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "$currentMonth collected",
                        value = "$${String.format(Locale.getDefault(), "%,.0f", collectedTotal)}",
                        subtitle = "$pendingCount accounts unpaid",
                        icon = Icons.Rounded.MonetizationOn,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Average Progress",
                        value = if (progressLogs.isNotEmpty()) String.format(Locale.getDefault(), "%.1f ★", avgRating) else "--",
                        subtitle = "${progressLogs.size} logs recorded",
                        icon = Icons.Rounded.Star,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Entries feed
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Recent Progress Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (progressLogs.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.HistoryEdu,
                                    contentDescription = "No recent records",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    "No progress records logged yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        progressLogs.take(3).forEach { log ->
                            val sName = students.find { it.id == log.studentId }?.name ?: "Unknown Student"
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectedStudentForDetail = students.find { it.id == log.studentId }
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Covered: ${log.topic}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (log.remarks.isNotEmpty()) {
                                            Text(
                                                text = "\"${log.remarks}\"",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        RatingStars(rating = log.rating, size = 16)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = log.dateString,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = tint)
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ==========================================
// 2. STUDENTS SCREEN
// ==========================================
@Composable
fun StudentsScreen(viewModel: TuitionViewModel, students: List<Student>) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf("All Class") }

    val filteredList = students.filter { student ->
        val nameMatches = student.name.contains(searchQuery, ignoreCase = true)
        val gradeMatches = student.grade.contains(searchQuery, ignoreCase = true)
        val subjectMatches = student.subject.contains(searchQuery, ignoreCase = true)
        val matchesQuery = nameMatches || gradeMatches || subjectMatches

        val matchesFilter = if (selectedGroupFilter == "All Class") {
            true
        } else {
            student.grade == selectedGroupFilter
        }
        matchesQuery && matchesFilter
    }

    // Get all grades for class-wise grouping filters
    val classes = remember(students) {
        val list = mutableListOf("All Class")
        list.addAll(students.map { it.grade }.distinct().sorted())
        list
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddStudentDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Student")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "My Students",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "${students.count { it.isActive }} Active Tutees",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search outliner
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, grade, subject...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                } else null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Horizontal filters for Classes
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(classes) { cls ->
                    val selected = selectedGroupFilter == cls
                    FilterChip(
                        selected = selected,
                        onClick = { selectedGroupFilter = cls },
                        label = { Text(cls) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // List displaying students
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Group,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "No students added yet!" else "No records match search queries.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Use the buttons on screen or floating + to register new students.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(filteredList) { student ->
                        StudentItemCard(
                            student = student,
                            onClick = { viewModel.selectedStudentForDetail = student },
                            onLogPayment = { viewModel.showAddPaymentDialogForStudent = student },
                            onLogProgress = { viewModel.showAddProgressDialogForStudent = student }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentItemCard(
    student: Student,
    onClick: () -> Unit,
    onLogPayment: () -> Unit,
    onLogProgress: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (!student.isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "Inactive",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(student.grade, style = MaterialTheme.typography.bodySmall) }
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text(student.subject, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        "$$" + String.format(Locale.getDefault(), "%,.0f", student.monthlyFee) + "/mo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (student.notes.isNotBlank()) {
                Text(
                    text = student.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call student direct button
                IconButton(
                    onClick = {
                        if (student.contactNumber.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${student.contactNumber}")
                            }
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "No contact number configured.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Rounded.Phone, contentDescription = "Call Student", modifier = Modifier.size(16.dp))
                }

                // Quick Logs Shortcut buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onLogProgress,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Rounded.Star, contentDescription = "Review", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Review", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = onLogPayment,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Rounded.AttachMoney, contentDescription = "Pay", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay Fee", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. ATTENDANCE REGISTER SCREEN
// ==========================================
@Composable
fun AttendanceScreen(
    viewModel: TuitionViewModel,
    students: List<Student>,
    sessionLogs: List<SessionLog>,
    selectedDate: String
) {
    val activeStudents = students.filter { it.isActive }
    val displayFormatInput = viewModel.apiDateFormat.parse(selectedDate) ?: Date()
    val fullDateDisplay = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(displayFormatInput)

    // Filter sessions on selected date
    val sessionsToday = sessionLogs.filter { it.dateString == selectedDate }
    val totalSessionsCount = sessionsToday.size
    val totalDurationMin = sessionsToday.sumOf { it.durationMinutes }
    val totalHrs = totalDurationMin / 60
    val totalMins = totalDurationMin % 60
    val formattedDuration = if (totalHrs > 0) "${totalHrs}h ${totalMins}m" else "${totalMins} mins"

    var showManualSessionDialogForStudentId by remember { mutableStateOf<Int?>(null) }
    var showGeneralManualSessionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Attendance Date navigation bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tutoring Sessions & Attendance Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.changeDate(-1) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Prev Day")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = "Date")
                        Text(
                            text = fullDateDisplay,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }

                    IconButton(onClick = { viewModel.changeDate(1) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next Day")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = { viewModel.setDate(Date()) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RESET TO TODAY")
                    }
                    
                    Button(
                        onClick = { showGeneralManualSessionDialog = true },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Log Session", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LOG SESSION", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Attendance / Session Metrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalSessionsCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Sessions Today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedDuration,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text("Tutoring Hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Checklist of sessions conducted
        Text(
            "Conducted Sessions today",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (sessionsToday.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tutoring sessions logged for this date.\nTap LOG SESSION above to manually record one.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(sessionsToday) { log ->
                    val student = activeStudents.find { it.id == log.studentId }
                    val studentName = student?.name ?: "Unknown Student"
                    val classSubject = student?.let { "${it.grade} • ${it.subject}" } ?: ""
                    
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val startTimeFormatted = timeFormat.format(Date(log.startTimeMillis))
                    val endTimeFormatted = timeFormat.format(Date(log.endTimeMillis))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = studentName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = classSubject,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Schedule,
                                        contentDescription = "Hours",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "$startTimeFormatted - $endTimeFormatted (${log.durationMinutes} mins)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (log.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Topic: ${log.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { 
                                    viewModel.deleteSessionLog(log)
                                    Toast.makeText(viewModel.getApplication(), "Deleted session log", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Quick Add check-sheet for active students who haven't logged on this date
            val studentIdsWithSessionsToday = sessionsToday.map { it.studentId }.toSet()
            val studentsNotLoggedToday = activeStudents.filter { it.id !in studentIdsWithSessionsToday }

            if (studentsNotLoggedToday.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Conduct Session / Quick Log",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                items(studentsNotLoggedToday) { std ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = std.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${std.grade} • ${std.subject}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { showManualSessionDialogForStudentId = std.id },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Quick Log", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Session", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogs for manual session log
    if (showGeneralManualSessionDialog) {
        AddManualSessionDialog(
            students = activeStudents,
            initialDateString = selectedDate,
            onDismiss = { showGeneralManualSessionDialog = false },
            onSave = { stdId, dateStr, startStr, endStr, rem ->
                viewModel.addManualSessionLog(stdId, dateStr, startStr, endStr, rem)
                showGeneralManualSessionDialog = false
                Toast.makeText(viewModel.getApplication(), "Logged tutoring session successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    showManualSessionDialogForStudentId?.let { preselectedId ->
        AddManualSessionDialog(
            students = activeStudents,
            preselectedStudentId = preselectedId,
            initialDateString = selectedDate,
            onDismiss = { showManualSessionDialogForStudentId = null },
            onSave = { stdId, dateStr, startStr, endStr, rem ->
                viewModel.addManualSessionLog(stdId, dateStr, startStr, endStr, rem)
                showManualSessionDialogForStudentId = null
                Toast.makeText(viewModel.getApplication(), "Tutoring session logged!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ==========================================
// 4. FEES SCREEN (PAYMENTS & ACCOUNTS)
// ==========================================
@Composable
fun FeesScreen(
    viewModel: TuitionViewModel,
    students: List<Student>,
    payments: List<FeePayment>
) {
    val activeStudents = students.filter { it.isActive }
    val currentMonthFilter by viewModel.selectedMonthFilter.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availablePaymentMonths.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Ledger Status, 1: History Log

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Tuition Fees",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Subtab selectors
        TabRow(selectedTabIndex = activeSubTab) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Monthly Status") }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Receipts History") }
            )
        }

        // Month selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Billing Period:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Month filtering dropdown/lazyrow list for easy select
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(availableMonths) { m ->
                    val selected = currentMonthFilter == m
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectedMonthFilter.value = m },
                        label = { Text(m) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        if (activeSubTab == 0) {
            // LEDGER STATUS (Check who hasn't paid)
            val currentPayments = payments.filter { it.monthCovered == currentMonthFilter }
            val paidMap = currentPayments.associateBy { it.studentId }

            if (activeStudents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Please add students before billing reports can be generated.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(activeStudents) { student ->
                        val payment = paidMap[student.id]
                        val hasPaid = payment != null

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        student.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        "Fee Rate: $${String.format(Locale.getDefault(), "%,.0f", student.monthlyFee)}/month",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (hasPaid && payment != null) {
                                        Text(
                                            "Collected: $$${String.format(Locale.getDefault(), "%,.0f", payment.amount)} on ${payment.paymentDateString}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (hasPaid) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE8F5E9))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = "Paid",
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    "Paid",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = Color(0xFF4CAF50),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.showAddPaymentDialogForStudent = student },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Rounded.Add, contentDescription = "Log", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Collector", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // RECEIPTS LOG HISTORY
            val filteredPayments = payments.filter { it.monthCovered == currentMonthFilter }

            if (filteredPayments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Receipt,
                            contentDescription = "Empty receipts",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            "No receipts recorded for $currentMonthFilter.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPayments) { payment ->
                        val student = students.find { it.id == payment.studentId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        student?.name ?: "Deleted Student",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        "Paid Date: ${payment.paymentDateString}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (payment.notes.isNotBlank()) {
                                        Text(
                                            "Note: ${payment.notes}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "$$" + String.format(Locale.getDefault(), "%,.0f", payment.amount),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteFeePayment(payment) },
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. PROGRESS LOGS SCREEN (REVIEWS)
// ==========================================
@Composable
fun ProgressScreen(
    viewModel: TuitionViewModel,
    students: List<Student>,
    progressLogs: List<ProgressLog>,
    examResults: List<ExamResult>
) {
    val activeStudents = students.filter { it.isActive }
    var selectedStudentFilter by remember { mutableStateOf<Student?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val filteredProgress = remember(progressLogs, selectedStudentFilter) {
        if (selectedStudentFilter == null) {
            progressLogs
        } else {
            progressLogs.filter { it.studentId == selectedStudentFilter!!.id }
        }
    }

    val filteredExams = remember(examResults, selectedStudentFilter) {
        if (selectedStudentFilter == null) {
            examResults
        } else {
            examResults.filter { it.studentId == selectedStudentFilter!!.id }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (activeStudents.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val sel = selectedStudentFilter ?: activeStudents.first()
                        if (selectedTab == 0) {
                            viewModel.showAddProgressDialogForStudent = sel
                        } else {
                            viewModel.showAddExamDialogForStudent = sel
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (selectedTab == 0) {
                        Icon(Icons.Rounded.PostAdd, contentDescription = "Add review")
                    } else {
                        Icon(Icons.Rounded.Assessment, contentDescription = "Add Exam Result")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Academic Tracking",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Horizontal filters for selecting student
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Student:",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedStudentFilter == null,
                            onClick = { selectedStudentFilter = null },
                            label = { Text("All Students") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                             )
                        )
                    }

                    items(activeStudents) { student ->
                        val selected = selectedStudentFilter?.id == student.id
                        FilterChip(
                            selected = selected,
                            onClick = { selectedStudentFilter = student },
                            label = { Text(student.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Material 3 TabRow to toggle progress vs exams
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Lesson Logs", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Rounded.AutoStories, contentDescription = "Lesson Logs") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Exams Monitor", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Rounded.AssignmentTurnedIn, contentDescription = "Exam Monitor") }
                )
            }

            if (selectedTab == 0) {
                // LESSON LOGS TAB
                if (filteredProgress.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AutoStories,
                                contentDescription = "Empty progress logs",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "No progress evaluations logged.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Log lessons, ratings, and custom feedback here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProgress) { log ->
                            val student = students.find { it.id == log.studentId }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                student?.name ?: "Deleted Student",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Evaluation Date: ${log.dateString}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        RatingStars(rating = log.rating, size = 18)
                                    }

                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Topic: ${log.topic}", style = MaterialTheme.typography.bodySmall) }
                                    )

                                    if (log.remarks.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = log.remarks,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.deleteProgressLog(log) },
                                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // EXAMS MONITORING TAB
                if (filteredExams.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Assignment,
                                contentDescription = "Empty exams",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "No exam results logged yet.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Click the floating button to log exam marks.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Show generic summary statistics if filtering a student
                        selectedStudentFilter?.let { student ->
                            val avgPct = remember(filteredExams) {
                                if (filteredExams.isEmpty()) 0.0 else {
                                    filteredExams.map { if (it.totalMarks > 0) (it.obtainedMarks / it.totalMarks) * 100 else 0.0 }.average()
                                }
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Academic Summary",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "${student.name}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "Average Marks",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = String.format(Locale.getDefault(), "%.1f%%", avgPct),
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (avgPct >= 80.0) Color(0xFF4CAF50) else if (avgPct >= 50.0) MaterialTheme.colorScheme.primary else Color(0xFFF44336)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredExams) { result ->
                                val student = students.find { it.id == result.studentId }
                                val percentage = if (result.totalMarks > 0) (result.obtainedMarks / result.totalMarks) * 100 else 0.0
                                val percentColor = if (percentage >= 80.0) Color(0xFF4CAF50) else if (percentage >= 50.0) MaterialTheme.colorScheme.primary else Color(0xFFF44336)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    result.subject,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    "${result.examName} • ${student?.name ?: "Deleted Student"}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    "Date: ${result.dateString}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }

                                            // Score display
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${result.obtainedMarks.toInt()}/${result.totalMarks.toInt()}",
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = percentColor)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(percentColor.copy(alpha = 0.1f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = String.format(Locale.getDefault(), "%.1f%%", percentage),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = percentColor)
                                                    )
                                                }
                                            }
                                        }

                                        if (result.remarks.isNotBlank()) {
                                            Text(
                                                text = "Remarks: " + result.remarks,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                    .padding(8.dp)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.deleteExamResult(result) },
                                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Delete,
                                                    contentDescription = "Delete Result",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// RATING COMPONENT
// ==========================================
@Composable
fun RatingStars(rating: Int, modifier: Modifier = Modifier, size: Int = 16) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = modifier) {
        for (i in 1..5) {
            val filled = i <= rating
            Icon(
                imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = "$rating stars",
                tint = if (filled) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(size.dp)
            )
        }
    }
}

// ==========================================
// DIALOG 1: ADD/EDIT STUDENT DIALOG
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentDialog(
    student: Student? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, grade: String, subject: String, monthlyFee: Double, phone: String, notes: String, paymentCycleDays: Int) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var grade by remember { mutableStateOf(student?.grade ?: "") }
    var subject by remember { mutableStateOf(student?.subject ?: "") }
    var monthlyFee by remember { mutableStateOf(student?.monthlyFee?.toString() ?: "") }
    var phone by remember { mutableStateOf(student?.contactNumber ?: "") }
    var notes by remember { mutableStateOf(student?.notes ?: "") }
    var paymentCycleDays by remember { mutableStateOf(student?.paymentCycleDays?.toString() ?: "12") }

    var nameError by remember { mutableStateOf(false) }
    var feeError by remember { mutableStateOf(false) }
    var cycleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (student == null) "Add Student" else "Edit Student Profile",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Student Name *") },
                    isError = nameError,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Class / Grade") },
                    placeholder = { Text("e.g. Class 10, GCSE, College") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Subject") },
                    placeholder = { Text("e.g. Mathematics, Calculus") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = monthlyFee,
                    onValueChange = {
                        monthlyFee = it
                        feeError = it.toDoubleOrNull() == null && it.isNotBlank()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Monthly Tuition Fee ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = feeError,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = paymentCycleDays,
                    onValueChange = {
                        paymentCycleDays = it.filter { char -> char.isDigit() }
                        cycleError = paymentCycleDays.toIntOrNull() == null || (paymentCycleDays.toIntOrNull() ?: 0) <= 0
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Payment Cycle Days (e.g., 12 days)") },
                    placeholder = { Text("Default 12 days a month") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = cycleError,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Parent/Student Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Remarks / Extra details") },
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val parsedFee = monthlyFee.toDoubleOrNull() ?: 0.0
                    val parsedCycle = paymentCycleDays.toIntOrNull() ?: 12
                    onSave(name, grade, subject, parsedFee, phone, notes, parsedCycle)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

// ==========================================
// DIALOG 2: ADD PAYMENT DIALOG
// ==========================================
@Composable
fun AddPaymentDialog(
    student: Student,
    viewModel: TuitionViewModel,
    onDismiss: () -> Unit,
    onSave: (amount: Double, month: String, notes: String) -> Unit
) {
    var amount by remember { mutableStateOf(student.monthlyFee.toString()) }
    var monthCovered by remember { mutableStateOf(viewModel.monthFormat.format(Date())) }
    var notes by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Record Fee Payment",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Student: ${student.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = it.toDoubleOrNull() == null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount Paid ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = monthCovered,
                    onValueChange = { monthCovered = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Month/Period Covered") },
                    placeholder = { Text("e.g. ${viewModel.monthFormat.format(Date())}") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes (Receipt ID or payment method)") },
                    placeholder = { Text("e.g. Cash, GPay, Bank Transfer") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null) {
                        amountError = true
                        return@Button
                    }
                    onSave(parsedAmount, monthCovered, notes)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("RECORD PAYMENT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

// ==========================================
// DIALOG 3: ADD PROGRESS DIALOG
// ==========================================
@Composable
fun AddProgressDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (topic: String, rating: Int, remarks: String) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }
    var remarks by remember { mutableStateOf("") }
    var topicError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Log Student Progress",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Student: ${student.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = {
                        topic = it
                        topicError = it.isBlank()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Concept/Topic covered today *") },
                    placeholder = { Text("e.g. Fractions, Newton's Laws") },
                    isError = topicError,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Understanding Rating:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 1..5) {
                            val active = i <= rating
                            Icon(
                                imageVector = if (active) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                contentDescription = "$i Star",
                                tint = if (active) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { rating = i }
                            )
                        }
                    }
                    val ratingLabel = when (rating) {
                        1 -> "Poor Comprehension - needs major revision"
                        2 -> "Trouble with concepts - revision needed"
                        3 -> "Average - understood basic formulas"
                        4 -> "Good Comprehension - minor difficulties"
                        else -> "Excellent! Grasped concepts perfectly!"
                    }
                    Text(
                        text = ratingLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Remarks / Homework Assigned") },
                    placeholder = { Text("e.g. Page 45 Ex A-D. Did well but needs practice.") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isBlank()) {
                        topicError = true
                        return@Button
                    }
                    onSave(topic, rating, remarks)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("LOG PROGRESS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

// ==========================================
// 6. STUDENT DETAIL SHEET OVERLAY (GORGEOUS DETAILED TIMELINE)
// ==========================================
@Composable
fun StudentDetailOverlay(
    student: Student,
    viewModel: TuitionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sessionHistory by viewModel.getSessionLogsForStudent(student.id).collectAsStateWithLifecycle(emptyList())
    val paymentHistory by viewModel.getFeePaymentsForStudent(student.id).collectAsStateWithLifecycle(emptyList())
    val progressHistory by viewModel.getProgressLogsForStudent(student.id).collectAsStateWithLifecycle(emptyList())

    var activeSubTab by remember { mutableStateOf(0) } // 0: History feed, 1: Accounts, 2: Sessions

    // Calculate details
    val totalSessions = sessionHistory.size
    val targetDays = student.paymentCycleDays
    val totalPaidCycles = paymentHistory.size
    val sessionsInCurrentCycle = maxOf(0, totalSessions - (totalPaidCycles * targetDays))
    val sessionsRemaining = maxOf(0, targetDays - sessionsInCurrentCycle)
    val isPaymentDue = sessionsInCurrentCycle >= targetDays

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with back navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Close detailed sheet")
                }
                Text(
                    text = "Student Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Row {
                    IconButton(onClick = { viewModel.studentToEdit = student }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit student")
                    }
                    IconButton(onClick = {
                        viewModel.deleteStudent(student)
                        Toast.makeText(context, "Student record deleted.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Student Overview profile summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            student.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "${student.grade} • ${student.subject}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isPaymentDue) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isPaymentDue) "PAYMENT DUE!" else "$sessionsRemaining sessions left",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPaymentDue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                    )
                                )
                            }
                            Text(
                                "Taught: $totalSessions",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (student.contactNumber.isNotBlank()) {
                                Text(
                                    "Ph: ${student.contactNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${student.contactNumber}")
                                        }
                                        context.startActivity(dialIntent)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            TabRow(selectedTabIndex = activeSubTab) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Progress") }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("Fees") }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text("Sessions") }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeSubTab) {
                    0 -> {
                        // PROGRESS HISTORY TAB
                        if (progressHistory.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(onClick = { viewModel.showAddProgressDialogForStudent = student }) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add First Progress Review")
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(progressHistory) { log ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    log.dateString,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                RatingStars(rating = log.rating, size = 14)
                                            }
                                            Text(
                                                "Covered: ${log.topic}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            if (log.remarks.isNotBlank()) {
                                                Text(
                                                    log.remarks,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // FEES PAYMENT LOG TAB
                        if (paymentHistory.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(onClick = { viewModel.showAddPaymentDialogForStudent = student }) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Record First Payment")
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(paymentHistory) { pay ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    "Paid for: ${pay.monthCovered}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    "Paid on ${pay.paymentDateString}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (pay.notes.isNotBlank()) {
                                                    Text(
                                                        pay.notes,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                            Text(
                                                "$$" + String.format(Locale.getDefault(), "%,.0f", pay.amount),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF4CAF50)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // SESSIONS HISTORICAL LOG TAB
                        if (sessionHistory.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No sessions logged for this student yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(sessionHistory) { log ->
                                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                    val startStr = timeFormat.format(Date(log.startTimeMillis))
                                    val endStr = timeFormat.format(Date(log.endTimeMillis))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    log.dateString,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        "${log.durationMinutes} mins",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    )
                                                }
                                            }
                                            Text(
                                                "$startStr to $endStr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (log.notes.isNotBlank()) {
                                                Text(
                                                    "Topic: ${log.notes}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// TUITION SCHEDULE SCREEN WITH LIVE TIMER AND CALENDAR
// ==========================================================================================
@Composable
fun ScheduleScreen(
    viewModel: TuitionViewModel,
    students: List<Student>,
    schedules: List<TuitionSchedule>,
    sessionLogs: List<SessionLog>,
    activeTimers: List<ActiveTimer>
) {
    var activeSubTab by remember { mutableStateOf(0) }
    val activeStudents = students.filter { it.isActive }

    Scaffold(
        floatingActionButton = {
            if (activeSubTab == 1 && activeStudents.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.showAddScheduleDialogForStudent = activeStudents.first() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Schedule")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Tutoring Schedule",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            TabRow(
                selectedTabIndex = activeSubTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("Calendar & Timers") },
                    icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Calendar/Timers") }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("Weekly Planner") },
                    icon = { Icon(Icons.Rounded.ViewWeek, contentDescription = "Weekly Matrix") }
                )
            }

            if (activeSubTab == 0) {
                // CALENDAR & TIMERS SECTION
                var selectedDateMs by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
                val selectedCalendarDate = remember(selectedDateMs) {
                    Calendar.getInstance().apply { timeInMillis = selectedDateMs }
                }
                
                // Track active ticker update
                var tickerUpdateTrigger by remember { mutableStateOf(0L) }
                LaunchedEffect(Unit) {
                    while (true) {
                        kotlinx.coroutines.delay(1000)
                        tickerUpdateTrigger = System.currentTimeMillis()
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Calendar Grid Card
                    item {
                        ScheduleCalendarView(
                            schedules = schedules,
                            sessionLogs = sessionLogs,
                            selectedDate = selectedCalendarDate,
                            onDateSelect = { newDate -> selectedDateMs = newDate.timeInMillis }
                        )
                    }

                    // 2. Active Persisted Timers Row
                    if (activeTimers.isNotEmpty()) {
                        item {
                            Text(
                                "Live Teaching Sessions",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        items(activeTimers) { timer ->
                            val student = students.find { it.id == timer.studentId }
                            val elapsedMs = if (tickerUpdateTrigger > timer.startTimeMillis) tickerUpdateTrigger - timer.startTimeMillis else 0L
                            val secs = (elapsedMs / 1000) % 60
                            val mins = (elapsedMs / 60000) % 60
                            val hrs = elapsedMs / 3600000
                            val clockStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "📖 Tutoring in Progress...",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                        )
                                        Text(
                                            student?.name ?: "Deleted Student",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (timer.notes.isNotBlank()) {
                                            Text(
                                                "Objective: ${timer.notes}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tutee duration: $clockStr",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Stop
                                        IconButton(
                                            onClick = { viewModel.stopTimerForStudent(timer.studentId) },
                                            modifier = Modifier.clip(CircleShape).background(Color(0xFFE53935)),
                                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Rounded.Stop, contentDescription = "Stop Session")
                                        }
                                        // Discard
                                        IconButton(
                                            onClick = { viewModel.cancelTimerForStudent(timer.studentId) },
                                            modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                        ) {
                                            Icon(Icons.Rounded.Cancel, contentDescription = "Cancel Session")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2b. Start an instantaneous Timer block
                    val availableForTimer = activeStudents.filter { st -> activeTimers.none { it.studentId == st.id } }
                    if (availableForTimer.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        "Launch Timer Session",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Track teaching time automatically in the database.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    var dropdownExpanded by remember { mutableStateOf(false) }
                                    var timerTargetStudent by remember { mutableStateOf(availableForTimer.first()) }
                                    var objectiveText by remember { mutableStateOf("") }

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = timerTargetStudent.name,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Select Student") },
                                            trailingIcon = {
                                                IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = "dropdown")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        DropdownMenu(
                                            expanded = dropdownExpanded,
                                            onDismissRequest = { dropdownExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.9f)
                                        ) {
                                            availableForTimer.forEach { st ->
                                                DropdownMenuItem(
                                                    text = { Text(st.name) },
                                                    onClick = {
                                                        timerTargetStudent = st
                                                        dropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = objectiveText,
                                        onValueChange = { objectiveText = it },
                                        placeholder = { Text("Teaching topic or logs goals today...") },
                                        label = { Text("Session Plan (Optional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            viewModel.startTimerForStudent(timerTargetStudent.id, objectiveText)
                                            objectiveText = ""
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Play")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Start Teaching Live")
                                    }
                                }
                            }
                        }
                    }

                    // 3. Selection Details
                    val formattedSelected = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendarDate.time)
                    val selectedDayName = SimpleDateFormat("EEEE", Locale.US).format(selectedCalendarDate.time)

                    item {
                        Text(
                            text = "History & Recurring on $selectedDayName ($formattedSelected)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Recurring schedules matching selected weekday name
                    val daySchedules = schedules.filter { it.dayOfWeek.equals(selectedDayName, ignoreCase = true) }
                    if (daySchedules.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    "No recurring tuition schedules set for ${selectedDayName}s.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(daySchedules) { sch ->
                            val student = students.find { it.id == sch.studentId }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            student?.name ?: "Deleted Student",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            "Time: ${sch.timeString} • Duration: ${sch.durationMinutes} mins",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (sch.notes.isNotBlank()) {
                                            Text(
                                                sch.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Recurring") },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Actual session Logs matching selected date
                    val daySessions = sessionLogs.filter { it.dateString == formattedSelected }
                    if (daySessions.isNotEmpty()) {
                        item {
                            Text(
                                "Actual Taught Sessions Logged Today",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(daySessions) { session ->
                            val student = students.find { it.id == session.studentId }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            student?.name ?: "Deleted Student",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            "Duration Taught: ${session.durationMinutes} minutes",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF4CAF50)
                                        )
                                        if (session.notes.isNotBlank()) {
                                            Text(
                                                "Session logs: ${session.notes}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteSessionLog(session) }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Delete Session Log",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // WEEKLY PLANNER MATRIX TAB
                val daysOrder = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                val hasSchedules = schedules.isNotEmpty()
                
                if (!hasSchedules) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                Icons.Rounded.Schedule,
                                contentDescription = "empty",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Text(
                                "No recurring weekly schedules created.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                "Click the + floating button to schedule weekly classes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(daysOrder) { day ->
                            val daySchedules = schedules.filter { it.dayOfWeek.equals(day, ignoreCase = true) }
                            if (daySchedules.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            daySchedules.forEach { sch ->
                                                val student = students.find { it.id == sch.studentId }
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                student?.name ?: "Deleted Student",
                                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                                            )
                                                            Text(
                                                                "⏱️ ${sch.timeString} (${sch.durationMinutes} mins)",
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                        }
                                                        IconButton(onClick = { viewModel.deleteSchedule(sch) }) {
                                                            Icon(
                                                                Icons.Rounded.Delete,
                                                                contentDescription = "Delete Schedule",
                                                                tint = MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom Grid Calendar View
@Composable
fun ScheduleCalendarView(
    schedules: List<TuitionSchedule>,
    sessionLogs: List<SessionLog>,
    selectedDate: Calendar,
    onDateSelect: (Calendar) -> Unit
) {
    val tempCal = selectedDate.clone() as Calendar
    tempCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
    val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells = ((maxDays + firstDayOfWeek + 6) / 7) * 7
    val dayNumberFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = {
                    val c = selectedDate.clone() as Calendar
                    c.add(Calendar.MONTH, -1)
                    onDateSelect(c)
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Prev")
                }
                IconButton(onClick = {
                    val c = selectedDate.clone() as Calendar
                    c.add(Calendar.MONTH, 1)
                    onDateSelect(c)
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next")
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
            days.forEach { d ->
                Text(
                    text = d,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        var cellIndex = 0
        while (cellIndex < totalCells) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayOfThisCell = cellIndex - firstDayOfWeek + 1
                    val isValidDay = dayOfThisCell in 1..maxDays

                    if (isValidDay) {
                        val cellCal = selectedDate.clone() as Calendar
                        cellCal.set(Calendar.DAY_OF_MONTH, dayOfThisCell)
                        val cellDayOfWeekString = SimpleDateFormat("EEEE", Locale.US).format(cellCal.time)
                        val cellDateString = dayNumberFormat.format(cellCal.time)

                        val isToday = dayOfThisCell == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                                selectedDate.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                                selectedDate.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)

                        val isSelected = cellCal.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH) &&
                                cellCal.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                                cellCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)

                        val hasSchedule = schedules.any { it.dayOfWeek.equals(cellDayOfWeekString, ignoreCase = true) }
                        val hasSession = sessionLogs.any { it.dateString == cellDateString }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isToday) 2.dp else 0.dp,
                                    color = if (isToday) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (hasSchedule) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else Color.Transparent
                                )
                                .clickable { onDateSelect(cellCal) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = "$dayOfThisCell",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else if (hasSchedule) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                if (hasSession) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else Color(0xFF4CAF50))
                                    )
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                    cellIndex++
                }
            }
        }
    }
}

// dialog for logging a schedule
@Composable
fun AddScheduleDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (day: String, time: String, duration: Int, notes: String) -> Unit
) {
    var selectedDay by remember { mutableStateOf("Saturday") }
    var timeStr by remember { mutableStateOf("04:30 PM") }
    var durationStr by remember { mutableStateOf("90") }
    var notesStr by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val daysOptions = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Schedule for ${student.name}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Day dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedDay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day of Week") },
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Choose day")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        daysOptions.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    selectedDay = d
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = timeStr,
                    onValueChange = { timeStr = it },
                    label = { Text("Start Time") },
                    placeholder = { Text("e.g. 04:30 PM") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it.filter { char -> char.isDigit() } },
                    label = { Text("Target Duration (mins)") },
                    placeholder = { Text("e.g. 90") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notesStr,
                    onValueChange = { notesStr = it },
                    label = { Text("Notes (Recurring goals)") },
                    placeholder = { Text("e.g. Homework checks first, then Math practice.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedDay, timeStr, durationStr.toIntOrNull() ?: 90, notesStr)
                }
            ) {
                Text("Save Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dialog for adding exams
@Composable
fun AddExamDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (subject: String, examName: String, total: Double, obtained: Double, remarks: String, date: Date) -> Unit
) {
    var subject by remember { mutableStateOf(student.subject) }
    var examName by remember { mutableStateOf("Midterm") }
    var totalMarks by remember { mutableStateOf("100") }
    var obtainedMarks by remember { mutableStateOf("85") }
    var remarks by remember { mutableStateOf("") }
    var examDateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Exam Result for ${student.name}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Exam Name") },
                    placeholder = { Text("e.g., Weekly Quiz 5, Term End, Quiz 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = obtainedMarks,
                        onValueChange = { obtainedMarks = it },
                        label = { Text("Marks") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = totalMarks,
                        onValueChange = { totalMarks = it },
                        label = { Text("Out of (Total)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = examDateStr,
                    onValueChange = { examDateStr = it },
                    label = { Text("Exam Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    placeholder = { Text("e.g., Excellent, needs math practice on algebra.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dateObj = try {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(examDateStr) ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }
                    onSave(
                        subject,
                        examName,
                        totalMarks.toDoubleOrNull() ?: 100.0,
                        obtainedMarks.toDoubleOrNull() ?: 0.0,
                        remarks,
                        dateObj
                    )
                }
            ) {
                Text("Add Marks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManualSessionDialog(
    students: List<Student>,
    preselectedStudentId: Int? = null,
    initialDateString: String = "",
    onDismiss: () -> Unit,
    onSave: (studentId: Int, dateString: String, startTimeStr: String, endTimeStr: String, notes: String) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.find { it.id == preselectedStudentId } ?: students.firstOrNull()) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var dateString by remember { mutableStateOf(if (initialDateString.isNotBlank()) initialDateString else SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var startTimeStr by remember { mutableStateOf("04:00 PM") }
    var endTimeStr by remember { mutableStateOf("05:30 PM") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Manual Session", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Student Dropdown Selection
                if (students.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Select Student", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedStudent?.name ?: "Select Student",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { dropdownExpanded = true }) {
                                        Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Dropdown")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                students.forEach { std ->
                                    DropdownMenuItem(
                                        text = { Text("${std.name} (${std.grade})") },
                                        onClick = {
                                            selectedStudent = std
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text("No students available. Add an active student first.")
                }

                // Date Fields
                DateFields(
                    initialDateString = dateString,
                    label = "Session Date",
                    onDateChanged = { dateString = it }
                )

                // Start Time fields
                TimeFields(
                    initialHour = 4,
                    initialMinute = 0,
                    initialIsAm = false,
                    label = "Starting Time",
                    onTimeChanged = { startTimeStr = it }
                )

                // End Time fields
                TimeFields(
                    initialHour = 5,
                    initialMinute = 30,
                    initialIsAm = false,
                    label = "Ending Time",
                    onTimeChanged = { endTimeStr = it }
                )

                // Optional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Session Description / Remarks") },
                    placeholder = { Text("e.g. Completed Chapter 5, Algebra practice.") },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stdId = selectedStudent?.id
                    if (stdId == null) {
                        return@Button
                    }
                    onSave(stdId, dateString, startTimeStr, endTimeStr, notes)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimeFields(
    initialHour: Int = 4,
    initialMinute: Int = 30,
    initialIsAm: Boolean = false,
    label: String = "Time",
    onTimeChanged: (String) -> Unit // returns "HH:MM AM/PM", e.g., "04:30 PM"
) {
    var hourText by remember { mutableStateOf(String.format("%02d", initialHour)) }
    var minuteText by remember { mutableStateOf(String.format("%02d", initialMinute)) }
    var isAm by remember { mutableStateOf(initialIsAm) }

    LaunchedEffect(hourText, minuteText, isAm) {
        val h = hourText.toIntOrNull() ?: 12
        val m = minuteText.toIntOrNull() ?: 0
        val suffix = if (isAm) "AM" else "PM"
        onTimeChanged(String.format("%02d:%02d %s", h, m, suffix))
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = hourText,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    if (clean.length <= 2) {
                        val num = clean.toIntOrNull()
                        if (num == null || (num in 1..12)) {
                            hourText = clean
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                placeholder = { Text("HH") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            
            Text(":", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = minuteText,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    if (clean.length <= 2) {
                        val num = clean.toIntOrNull()
                        if (num == null || (num in 0..59)) {
                            minuteText = clean
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                placeholder = { Text("MM") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // AM/PM selection
            Row(
                modifier = Modifier
                    .weight(1.5f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAm) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isAm = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "AM",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isAm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isAm) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isAm = false }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "PM",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (!isAm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DateFields(
    initialDateString: String = "", // format: "YYYY-MM-DD"
    label: String = "Date",
    onDateChanged: (String) -> Unit // returns "YYYY-MM-DD"
) {
    val calendar = remember {
        Calendar.getInstance().apply {
            if (initialDateString.isNotBlank()) {
                try {
                    val parts = initialDateString.split("-")
                    set(Calendar.YEAR, parts[0].toInt())
                    set(Calendar.MONTH, parts[1].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }
    var yearText by remember { mutableStateOf(calendar.get(Calendar.YEAR).toString()) }
    var monthText by remember { mutableStateOf(String.format("%02d", calendar.get(Calendar.MONTH) + 1)) }
    var dayText by remember { mutableStateOf(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))) }

    LaunchedEffect(yearText, monthText, dayText) {
        val y = yearText.toIntOrNull() ?: 2026
        val m = monthText.toIntOrNull() ?: 1
        val d = dayText.toIntOrNull() ?: 1
        onDateChanged(String.format("%04d-%02d-%02d", y, m, d))
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Day
            OutlinedTextField(
                value = dayText,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    if (clean.length <= 2) {
                        val num = clean.toIntOrNull()
                        if (num == null || (num in 1..31)) {
                            dayText = clean
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                label = { Text("Day") },
                placeholder = { Text("DD") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Month
            OutlinedTextField(
                value = monthText,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    if (clean.length <= 2) {
                        val num = clean.toIntOrNull()
                        if (num == null || (num in 1..12)) {
                            monthText = clean
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                label = { Text("Month") },
                placeholder = { Text("MM") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Year
            OutlinedTextField(
                value = yearText,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    if (clean.length <= 4) {
                        yearText = clean
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1.5f),
                label = { Text("Year") },
                placeholder = { Text("YYYY") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}
