package com.pratik.learning.familyTree.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.pratik.learning.familyTree.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
): ViewModel() {

    val products = productRepository
        .getPagedProducts()
        .cachedIn(viewModelScope)
}