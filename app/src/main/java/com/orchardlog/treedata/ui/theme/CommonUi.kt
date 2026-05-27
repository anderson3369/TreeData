package com.orchardlog.treedata.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
fun FieldGroup(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        if (title != null) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
            )
        }
        Card(
            elevation = 0.dp,
            shape = RoundedCornerShape(10.dp),
            backgroundColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun FieldRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    showChevron: Boolean = true,
    valueColor: Color = MaterialTheme.colors.primary,
    isPill: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = if (isPill) 6.dp else 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            color = Color.Black
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isPill) {
                Surface(
                    color = Color(0xFFE9E9EB),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = value,
                        fontSize = 17.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Text(
                    text = value,
                    fontSize = 17.sp,
                    color = valueColor,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            if (showChevron && !isPill) {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null,
                    tint = Color(0xFFC7C7CC),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DateTimeRow(
    label: String,
    dateValue: String,
    timeValue: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            color = Color.Black
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color(0xFFE9E9EB),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.clickable(onClick = onDateClick)
            ) {
                Text(
                    text = dateValue,
                    fontSize = 17.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = Color(0xFFE9E9EB),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.clickable(onClick = onTimeClick)
            ) {
                Text(
                    text = timeValue,
                    fontSize = 17.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun TransparentInputField(
    label: String,
    value: String,
    isNumber: Boolean = false,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, color = Color.Gray) },
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colors.primary
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 17.sp)
    )
}


fun dateToShortString(date: LocalDate): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "$month ${date.dayOfMonth}, ${date.year}"
}

fun timeToShortString(time: LocalTime): String {
    val hour = if (time.hour % 12 == 0) 12 else time.hour % 12
    val amPm = if (time.hour < 12) "AM" else "PM"
    return "$hour:${time.minute.toString().padStart(2, '0')} $amPm"
}
