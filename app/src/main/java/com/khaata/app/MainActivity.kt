package com.khaata.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.khaata.app.ui.CameraScreen
import com.khaata.app.ui.MainScreen
import com.khaata.app.ui.SummaryScreen
import com.khaata.app.ui.theme.KhaataTheme

class MainActivity : ComponentActivity() {

    private val viewModel: KhaataViewModel by viewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))

        setContent {
            KhaataTheme {
                val screen by viewModel.screen.collectAsState()
                when (screen) {
                    Screen.MAIN -> MainScreen(viewModel)
                    Screen.CAMERA -> CameraScreen(viewModel)
                    Screen.SUMMARY -> SummaryScreen(viewModel)
                }
            }
        }
    }
}
