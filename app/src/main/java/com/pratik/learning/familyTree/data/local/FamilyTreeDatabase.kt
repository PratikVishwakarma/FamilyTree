package com.pratik.learning.familyTree.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pratik.learning.familyTree.utils.BACKUP_DATA_STRING
import com.pratik.learning.familyTree.utils.BackupUtils.parseBackupData
import com.pratik.learning.familyTree.data.local.converter.Converters
import com.pratik.learning.familyTree.data.local.dao.FamilyTreeDao
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FamilyRelation::class, FamilyMember::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FamilyTreeDatabase : RoomDatabase() {

    abstract fun familyTreeDao(): FamilyTreeDao



    companion object {
        val DATABASE_NAME = "family_tree_db"
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to existing topics table
                db.execSQL("ALTER TABLE members ADD COLUMN isNewEntry INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE relations ADD COLUMN isNewEntry INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to existing topics table
                db.execSQL("ALTER TABLE relations ADD COLUMN dom TEXT NOT NULL DEFAULT ''")
            }
        }
    }

}