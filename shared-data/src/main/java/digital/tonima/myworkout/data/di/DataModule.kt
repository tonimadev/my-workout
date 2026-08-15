package digital.tonima.myworkout.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import digital.tonima.myworkout.data.local.AchievementDao
import digital.tonima.myworkout.data.local.AppDatabase
import digital.tonima.myworkout.data.local.WorkoutDao
import digital.tonima.myworkout.data.local.WorkoutSessionDao
import digital.tonima.myworkout.data.repository.*
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(workoutRepositoryImpl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindGamificationRepository(
        gamificationRepositoryImpl: GamificationRepositoryImpl,
    ): GamificationRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context,
        ): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "my_workout_db",
            ).fallbackToDestructiveMigration(false).build()
        }

        @Provides
        fun provideWorkoutDao(database: AppDatabase): WorkoutDao = database.workoutDao()

        @Provides
        fun provideWorkoutSessionDao(database: AppDatabase): WorkoutSessionDao = database.workoutSessionDao()

        @Provides
        fun provideAchievementDao(database: AppDatabase): AchievementDao = database.achievementDao()

        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> = context.dataStore
    }
}
