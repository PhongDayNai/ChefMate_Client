package com.watb.chefmate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color

private val ChefMateTextFieldTextColor = Color(0xFF1B1B1D)
private val ChefMateTextFieldPlaceholderColor = Color(0xFFADAEBC)
private val ChefMateTextFieldLabelColor = Color(0xFF5A5A60)
private val ChefMateTextFieldCursorColor = Color(0xFFFB923C)

@Composable
fun chefMateTextFieldColors(
    focusedTextColor: Color = ChefMateTextFieldTextColor,
    unfocusedTextColor: Color = ChefMateTextFieldTextColor,
    cursorColor: Color = ChefMateTextFieldCursorColor,
    focusedPlaceholderColor: Color = ChefMateTextFieldPlaceholderColor,
    unfocusedPlaceholderColor: Color = ChefMateTextFieldPlaceholderColor,
    focusedLabelColor: Color = ChefMateTextFieldLabelColor,
    unfocusedLabelColor: Color = ChefMateTextFieldLabelColor,
    focusedContainerColor: Color = Color.White,
    unfocusedContainerColor: Color = Color.White,
    focusedIndicatorColor: Color = Color.Transparent,
    unfocusedIndicatorColor: Color = Color.Transparent
): TextFieldColors = TextFieldDefaults.colors(
    focusedTextColor = focusedTextColor,
    unfocusedTextColor = unfocusedTextColor,
    cursorColor = cursorColor,
    focusedPlaceholderColor = focusedPlaceholderColor,
    unfocusedPlaceholderColor = unfocusedPlaceholderColor,
    focusedLabelColor = focusedLabelColor,
    unfocusedLabelColor = unfocusedLabelColor,
    focusedContainerColor = focusedContainerColor,
    unfocusedContainerColor = unfocusedContainerColor,
    focusedIndicatorColor = focusedIndicatorColor,
    unfocusedIndicatorColor = unfocusedIndicatorColor
)

@Composable
fun chefMateOutlinedTextFieldColors(
    focusedTextColor: Color = ChefMateTextFieldTextColor,
    unfocusedTextColor: Color = ChefMateTextFieldTextColor,
    cursorColor: Color = ChefMateTextFieldCursorColor,
    focusedPlaceholderColor: Color = ChefMateTextFieldPlaceholderColor,
    unfocusedPlaceholderColor: Color = ChefMateTextFieldPlaceholderColor,
    focusedLabelColor: Color = ChefMateTextFieldLabelColor,
    unfocusedLabelColor: Color = ChefMateTextFieldLabelColor,
    focusedContainerColor: Color = Color.White,
    unfocusedContainerColor: Color = Color.White,
    focusedBorderColor: Color = Color(0xFFE0E0E0),
    unfocusedBorderColor: Color = Color(0xFFE0E0E0)
): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = focusedTextColor,
    unfocusedTextColor = unfocusedTextColor,
    cursorColor = cursorColor,
    focusedPlaceholderColor = focusedPlaceholderColor,
    unfocusedPlaceholderColor = unfocusedPlaceholderColor,
    focusedLabelColor = focusedLabelColor,
    unfocusedLabelColor = unfocusedLabelColor,
    focusedContainerColor = focusedContainerColor,
    unfocusedContainerColor = unfocusedContainerColor,
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor
)
