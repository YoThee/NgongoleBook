package com.zuct.ngongolebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zuct.ngongolebook.ui.navigation.AppNavHost
import com.zuct.ngongolebook.ui.theme.NgongoleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NgongoleTheme {
                AppNavHost()
            }
        }
    }
}
