package com.mobileserver.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileserver.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 文件预览页：用 WebView 打开 OpenList 的原生预览地址，
 * 复用 OpenList 自带的图片/视频/音乐/文档在线查看能力。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewScreen(url: String, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { androidx.compose.foundation.layout.Column {
                    Text("在线预览", style = MaterialTheme.typography.titleMedium)
                    Text(url, fontSize = 11.sp, color = MiuiTextSecondary, maxLines = 1)
                } },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MiuiSurface)
            )
        }
    ) { padding ->
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    webViewClient = WebViewClient()
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    loadUrl(url)
                }
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MiuiBackground)
        )
    }
}
