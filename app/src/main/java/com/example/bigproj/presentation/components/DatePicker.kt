// presentation/components/DatePicker.kt
package com.example.bigproj.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: String? = null,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Parse initial date if provided (format: yyyy-MM-dd)
    val initialCalendar = remember {
        val cal = Calendar.getInstance()
        initialDate?.let { dateStr ->
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                format.parse(dateStr)?.let { date ->
                    cal.time = date
                }
            } catch (e: Exception) {
                // Use current date if parsing fails
            }
        }
        cal
    }

    var selectedYear by remember { mutableStateOf(initialCalendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(initialCalendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Выберите дату",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Month/Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectedMonth == 0) {
                            selectedMonth = 11
                            selectedYear--
                        } else {
                            selectedMonth--
                        }
                    }) {
                        Text("◀", fontSize = 18.sp)
                    }

                    var showMonthYearPicker by remember { mutableStateOf(false) }
                    
                    TextButton(onClick = { showMonthYearPicker = true }) {
                        val monthNames = arrayOf(
                            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
                        )
                        Text(
                            text = "${monthNames[selectedMonth]} $selectedYear",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = {
                        if (selectedMonth == 11) {
                            selectedMonth = 0
                            selectedYear++
                        } else {
                            selectedMonth++
                        }
                    }) {
                        Text("▶", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calendar grid
                val daysInMonth = remember(selectedYear, selectedMonth) {
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }.getActualMaximum(Calendar.DAY_OF_MONTH)
                }
                
                val firstDayOfWeek = remember(selectedYear, selectedMonth) {
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedYear)
                        set(Calendar.MONTH, selectedMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }.let { cal ->
                        (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
                    }
                }

                // Days of week header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar dates
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var currentDay = 1 - firstDayOfWeek
                    repeat(6) { // 6 weeks max
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            repeat(7) {
                                val day = currentDay
                                val isCurrentMonth = day in 1..daysInMonth
                                val isSelected = isCurrentMonth && day == selectedDay

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCurrentMonth) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    if (isSelected) Color(0xFF2196F3) else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    selectedDay = day
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                fontSize = 14.sp,
                                                color = if (isSelected) Color.White else Color.Black,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    } else {
                                        // Show days from previous/next month in gray
                                        val prevMonthDays = if (day < 1) {
                                            Calendar.getInstance().apply {
                                                set(Calendar.YEAR, selectedYear)
                                                set(Calendar.MONTH, selectedMonth)
                                                add(Calendar.MONTH, -1)
                                            }.getActualMaximum(Calendar.DAY_OF_MONTH)
                                        } else null
                                        
                                        val displayDay = if (day < 1 && prevMonthDays != null) {
                                            prevMonthDays + day
                                        } else if (day > daysInMonth) {
                                            day - daysInMonth
                                        } else {
                                            null
                                        }
                                        
                                        if (displayDay != null) {
                                            Text(
                                                text = displayDay.toString(),
                                                fontSize = 14.sp,
                                                color = Color(0xFFCCCCCC)
                                            )
                                        }
                                    }
                                }
                                currentDay++
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = {
                            // Очищаем выбранную дату
                            val today = Calendar.getInstance()
                            selectedYear = today.get(Calendar.YEAR)
                            selectedMonth = today.get(Calendar.MONTH)
                            selectedDay = today.get(Calendar.DAY_OF_MONTH)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена") // Изменяем текст с "Очистить" на "Отмена"
                    }
                    TextButton(
                        onClick = {
                            val today = Calendar.getInstance()
                            selectedYear = today.get(Calendar.YEAR)
                            selectedMonth = today.get(Calendar.MONTH)
                            selectedDay = today.get(Calendar.DAY_OF_MONTH)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Сегодня")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val dateCalendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, selectedYear)
                            set(Calendar.MONTH, selectedMonth)
                            set(Calendar.DAY_OF_MONTH, selectedDay)
                        }
                        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onDateSelected(format.format(dateCalendar.time))
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выбрать")
                }
            }
        }
    }
}
