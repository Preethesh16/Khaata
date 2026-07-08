package com.khaata.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.khaata.app.ui.CameraScreen
import com.khaata.app.ui.MainScreen
import com.khaata.app.ui.SummaryScreen
import com.khaata.app.ui.theme.KhaataTheme
import com.khaata.app.viewmodel.BillViewModel

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))

        setContent {
            KhaataTheme {
                val nav = rememberNavController()
                val vm: BillViewModel = viewModel()
                val goSummary by vm.navigateToSummary.collectAsState()

                LaunchedEffect(goSummary) {
                    if (goSummary) {
                        nav.navigate("summary")
                        vm.summaryShown()
                    }
                }

                NavHost(navController = nav, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            vm = vm,
                            onScan = { nav.navigate("camera") },
                            onDone = { vm.finishBill() }
                        )
                    }
                    composable("camera") {
                        CameraScreen(vm = vm, onBack = { nav.popBackStack() })
                    }
                    composable("summary") {
                        SummaryScreen(
                            vm = vm,
                            onNewBill = {
                                vm.clearBill()
                                nav.popBackStack("main", inclusive = false)
                            },
                            onBack = { nav.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
