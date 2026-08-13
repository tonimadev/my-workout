package digital.tonima.myworkout.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.tonima.myworkout.data.wearable.WearableSyncManager
import digital.tonima.myworkout.wearable.PhoneWearableSyncManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WearableModule {
    @Binds
    @Singleton
    abstract fun bindWearableSyncManager(phoneWearableSyncManager: PhoneWearableSyncManager): WearableSyncManager
}
