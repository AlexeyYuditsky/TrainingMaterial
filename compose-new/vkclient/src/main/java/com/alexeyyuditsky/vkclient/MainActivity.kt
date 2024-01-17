package com.alexeyyuditsky.vkclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.alexeyyuditsky.vkclient.ui.MainScreen
import com.alexeyyuditsky.vkclient.ui.FeedPostsViewModel
import com.alexeyyuditsky.vkclient.ui.theme.VkClientTheme

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<FeedPostsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VkClientTheme {
                MainScreen(viewModel)
            }
        }
    }
}