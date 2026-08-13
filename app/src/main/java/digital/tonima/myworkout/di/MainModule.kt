package digital.tonima.myworkout.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.tonima.myworkout.data.util.AlertManager
import digital.tonima.myworkout.util.PhoneAlertManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MainModule {
    @Binds
    @Singleton
    abstract fun bindAlertManager(phoneAlertManager: PhoneAlertManager): AlertManager
}
