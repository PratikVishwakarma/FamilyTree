package com.pratik.learning.familyTree.di

import android.content.Context
import androidx.room.Room
import com.pratik.learning.familyTree.data.local.FamilyTreeDatabase
import com.pratik.learning.familyTree.data.local.FamilyTreeDatabase.Companion.DATABASE_NAME
import com.pratik.learning.familyTree.data.local.FamilyTreeDatabase.Companion.MIGRATION_1_2
import com.pratik.learning.familyTree.data.local.FamilyTreeDatabase.Companion.MIGRATION_3_4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FamilyTreeDatabase {
        return Room
            .databaseBuilder(
                context,
                FamilyTreeDatabase::class.java,
                DATABASE_NAME)
//            .addMigrations(MIGRATION_1_2)
//            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigration()
//            .addCallback(FamilyTreeDatabase.DatabasePrePopulationCallback(context))
//            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideFamilyTreeDao(database: FamilyTreeDatabase) = database.familyTreeDao()

}