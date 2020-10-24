package al.ahgitdevelopment.municion.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
class SharedPrefsModule {

    @Provides
    fun provideSharedPrefs(@ApplicationContext appContext: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    companion object {
        const val PREFS_PASSWORD = "password"
        const val PREFS_SHOW_TUTORIAL = "tutorial"
    }
}
