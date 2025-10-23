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
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FamilyTreeDatabase : RoomDatabase() {

    abstract fun familyTreeDao(): FamilyTreeDao

    /**
     * Callback class to handle the one-time data pre-population on database creation.
     * MUST be internal/public for the Hilt module to access it.
     */
    internal class DatabasePrePopulationCallback(
        private val context: Context
    ) : Callback() {
        /**
         * Called when the database is created for the first time.
         * This is where we load the initial backup data.
         */
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Run the insertion on an IO Dispatcher, as database operations are heavy.
            CoroutineScope(Dispatchers.IO).launch {

                // NOTE: We must use Room.databaseBuilder here to get a temporary instance
                // and access the DAO. We cannot use the Hilt-provided instance because
                // the Hilt provision hasn't completed yet (it's currently building the DB).
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    FamilyTreeDatabase::class.java,
                    DATABASE_NAME // Uses the updated name
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()


                val dao = database.familyTreeDao()

                try {
                    // 1. Parse the static JSON backup string into Kotlin entities
                    val (members, relations) = parseBackupData(BACKUP_DATA_STRING)

                    // 2. Insert all data in a single transaction for atomicity and speed
                    dao.insertAllMembers(members)
                    dao.insertAllRelations(relations)

                } catch (e: Exception) {
                    // Note: In a real app, use Timber or a proper logging mechanism
                    println("Error loading initial backup data: ${e.message}")
                }
            }
        }
    }

    companion object {
        val DATABASE_NAME = "family_tree_db"
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to existing topics table
                db.execSQL("ALTER TABLE members ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE members  ADD COLUMN updatedBy TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE relations ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE relations  ADD COLUMN updatedBy TEXT NOT NULL DEFAULT ''")
//                // Create interview_questions table
//                db.execSQL(
//                    """
//            CREATE TABLE IF NOT EXISTS InterviewQuestion (
//                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                topicId INTEGER NOT NULL,
//                question TEXT NOT NULL,
//                answer TEXT NOT NULL
//            )
//        """.trimIndent()
//                )
            }
        }


        val MIGRATION_3_4: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to existing topics table
                db.execSQL("ALTER TABLE members ADD COLUMN gotra TEXT NOT NULL DEFAULT 'Khedavdiya'")
                db.execSQL("ALTER TABLE members ADD COLUMN state TEXT NOT NULL DEFAULT 'Madhya Pradesh'")

            }
        }
    }

}