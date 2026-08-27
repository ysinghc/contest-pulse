package org.iiitu.acm.contestpulse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.iiitu.acm.contestpulse.data.model.Contest

@Database(entities = [Contest::class], version = 1, exportSchema = false)
abstract class ContestDatabase : RoomDatabase() {

    abstract fun contestDao(): ContestDao

    companion object {
        @Volatile
        private var INSTANCE: ContestDatabase? = null

        fun getDatabase(context: Context): ContestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContestDatabase::class.java,
                    "contest_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
