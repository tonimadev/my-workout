package digital.tonima.myworkout.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE master_exercises ADD COLUMN primaryMuscle TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE master_exercises ADD COLUMN secondaryMuscles TEXT NOT NULL DEFAULT ''")
        }
    }
