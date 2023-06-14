package rzavodsky.planner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Database, which stores PlanBlocks
 */
@Database(entities = [PlanBlock::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PlanBlockDatabase: RoomDatabase() {
    abstract val planBlockDao: PlanBlockDao

    companion object {
        @Volatile
        private var INSTANCE: PlanBlockDatabase? = null

        /**
         * Returns the instance of this database singleton
         */
        fun getInstance(context: Context): PlanBlockDatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        PlanBlockDatabase::class.java,
                        "plan_block_database")
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return INSTANCE!!
            }
        }
    }
}