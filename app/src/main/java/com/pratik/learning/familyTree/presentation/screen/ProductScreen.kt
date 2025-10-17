package com.pratik.learning.familyTree.presentation.screen

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.pratik.learning.familyTree.presentation.component.TopicTile
import com.pratik.learning.familyTree.presentation.viewmodel.ProductViewModel

@Composable
fun ProductScreen(
    onProductClick: (Int, String) -> Unit,
    viewModel: ProductViewModel) {

    val products = viewModel.products.collectAsLazyPagingItems()

    LazyColumn {
        items(products.itemCount) { index ->
            val topic = products[index]
            topic?.let {
                TopicTile(
                    title = it.question,
                    description = it.answer,
                    onClick = { onProductClick(it.id, it.answer) }
                )
            }
        }
        products.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    // Initial load
                    item { CircularProgressIndicator() }
                }
                loadState.append is LoadState.Loading -> {
                    // Pagination load
                    item { CircularProgressIndicator() }
                }
                loadState.append is LoadState.Error -> {
                    // Handle initial load error
                    Log.d("ProductScreen exception", "${loadState.hasError}")
                    item {
                        Text("Load error. Try again.", color = Color.White)
                    }
                }
            }
        }
    }
}