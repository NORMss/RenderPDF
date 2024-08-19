package com.norm.myrenderpdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.norm.myrenderpdf.ui.theme.MyRenderPDFTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyRenderPDFTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PdfViewerScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding
                            )
                    )
                }
            }
        }
    }
}