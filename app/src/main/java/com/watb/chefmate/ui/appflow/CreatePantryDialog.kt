package com.watb.chefmate.ui.appflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watb.chefmate.R
import com.watb.chefmate.ui.theme.CustomTextField

@Composable
fun CreatePantryDialog(
    userId: Int,
    onDismiss: () -> Unit,
    onCreated: (pantryId: Int, name: String) -> Unit,
    onCreate: (userId: Int, name: String) -> Unit
) {
    var pantryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.create_pantry_title),
                color = Color(0xFF111827),
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.create_pantry_name_label),
                    color = Color(0xFF374151),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium))
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = pantryName,
                    onValueChange = { pantryName = it },
                    placeholder = stringResource(R.string.create_pantry_name_placeholder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pantryName.isNotBlank()) {
                        onCreate(userId, pantryName.trim())
                    }
                },
                enabled = pantryName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF97316),
                    disabledContainerColor = Color(0xFFFED7AA)
                )
            ) {
                Text(
                    text = stringResource(R.string.common_create),
                    color = Color.White,
                    fontFamily = FontFamily(Font(resId = R.font.roboto_bold))
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    color = Color(0xFF6B7280),
                    fontFamily = FontFamily(Font(resId = R.font.roboto_medium))
                )
            }
        },
        containerColor = Color(0xFFFFFBF8)
    )
}