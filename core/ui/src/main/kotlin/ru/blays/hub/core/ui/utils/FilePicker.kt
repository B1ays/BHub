package ru.blays.hub.core.ui.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun <I, O: Uri?> FilePicker(
    contract: ActivityResultContract<I, O>,
    pickFile: Boolean,
    input: I,
    onFilePicked: (O?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = contract,
        onResult = onFilePicked
    )
    LaunchedEffect(pickFile) {
        if(pickFile) {
            launcher.launch(input)
        }
    }
}