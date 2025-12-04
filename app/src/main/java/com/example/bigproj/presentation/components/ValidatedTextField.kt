// presentation/components/ValidatedTextField.kt
package com.example.bigproj.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bigproj.domain.validation.ValidationResult

@Composable
fun ValidatedEmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    validationResult: ValidationResult,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Электронная почта") },
            isError = validationResult.isError,
            modifier = Modifier.fillMaxWidth()
        )

        if (validationResult.isError) {
            Text(
                text = (validationResult as ValidationResult.Error).message,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ValidatedNameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    validationResult: ValidationResult,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Полное имя") },
            isError = validationResult.isError,
            modifier = Modifier.fillMaxWidth()
        )

        if (validationResult.isError) {
            Text(
                text = (validationResult as ValidationResult.Error).message,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}