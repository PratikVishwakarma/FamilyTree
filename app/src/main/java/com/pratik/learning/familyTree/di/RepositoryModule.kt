package com.pratik.learning.familyTree.di

import com.pratik.learning.familyTree.data.local.dao.FamilyTreeDao
import com.pratik.learning.familyTree.data.network.ApiService
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepositoryImpl
import com.pratik.learning.familyTree.data.repository.ProductRepository
import com.pratik.learning.familyTree.data.repository.ProductRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFamilyRepository(dao: FamilyTreeDao): FamilyTreeRepository {
        return FamilyTreeRepositoryImpl(dao)
    }


    @Provides
    @Singleton
    fun provideProductRepository(apiService: ApiService): ProductRepository {
        return ProductRepositoryImpl(apiService)
    }

}
