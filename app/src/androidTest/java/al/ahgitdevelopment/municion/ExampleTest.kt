package al.ahgitdevelopment.municion

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

// @UninstallModules()
@ExperimentalCoroutinesApi
@HiltAndroidTest
@LargeTest
class ExampleTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var someString: String

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun hiltTest() {
        ViewMatchers.assertThat(someString, CoreMatchers.containsString("TEST string"))
    }

    @Test
    fun mainActivityTest() {
        // val activityScenario = launchActivity<NavigationActivity>()
        val activityScenario = ActivityScenario.launch(NavigationActivity::class.java)
    }

    @Test
    fun mainFragmentTest() {

        // val scenario = launchFragmentInHiltContainer<LoginPasswordFragment>{}
    }

    @Module
    @InstallIn(ApplicationComponent::class)
    object ProductionModule {

        @Singleton
        @Provides
        fun provideString(): String {
            return "This is a TEST string I'm providing for injection"
        }
    }
}
