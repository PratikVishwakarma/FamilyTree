package com.pratik.learning.familyTree.di

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.pratik.learning.familyTree.data.local.dao.FamilyTreeDao
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepository
import com.pratik.learning.familyTree.data.repository.FamilyTreeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideFamilyRepository(dao: FamilyTreeDao, storage: FirebaseStorage, @ApplicationContext context: Context): FamilyTreeRepository {
        return FamilyTreeRepositoryImpl(dao, storage, context)
    }

}
