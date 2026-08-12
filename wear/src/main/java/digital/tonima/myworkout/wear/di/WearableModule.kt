package digital.tonima.myworkout.wear.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import digital.tonima.myworkout.wear.WatchWearableSyncManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WearableModule {
    @Binds
    @Singleton
    abstract fun bindWearableSyncManager(
        watchWearableSyncManager: WatchWearableSyncManager
    ): WearableSyncManager
}
