package com.example.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // The application id for the running build variant is read from the instrumentation arguments.
        rule.collect(
            packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),

            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll through your most important UI.

            // Start default activity for your app
            pressHome()
            startActivityAndWait()

            // TODO Write more interactions to optimize advanced journeys of your app.
            // For example:
            // 1. Wait until the content is asynchronously loaded
            waitForAsyncContent()
            // 2. Scroll the feed content
            scrollSnackListJourney()
            // 3. Navigate to detail screen
            goToSnackDetailJourney()


            // Check UiAutomator documentation for more information how to interact with the app.
            // https://d.android.com/training/testing/other-components/ui-automator
        }
    }
}
/**
 * snack_list > snack_collection > snack_item
 */
/**
 * ```kotlin
 * LazyColumn(
 *     modifier = Modifier.testTag("snack_list"),
 * ) ... itemsIndexed() { "snack_collection" }
 * ```
 *
 * 컨텐츠가 로드되고 렌더링될 때를 시스템에 알리고, 사용자가 실제 컨텐츠와 상호작용할 수 있는 앱 시작 시간을 기다림
 */
fun MacrobenchmarkScope.waitForAsyncContent() {
    device.wait(Until.hasObject(By.res("snack_list")), 5_000)
    val contentList = device.findObject(By.res("snack_list"))

    contentList.wait(Until.hasObject(By.res("snack_collection")), 5_000)
}

fun MacrobenchmarkScope.scrollSnackListJourney() {
    val snackList = device.findObject(By.res("snack_list"))

    snackList.setGestureMargin(device.displayWidth / 5)  // 시스템 탐색을 트리거하지 않도록 동작 여백을 설정합니다.
    snackList.fling(Direction.DOWN)
    device.waitForIdle()
}

fun MacrobenchmarkScope.goToSnackDetailJourney() {
    val snackList = device.findObject(By.res("snack_list"))
    val snacks = snackList.findObjects(By.res("snack_item"))

    // 목록에서 항목을 선택합니다.
    val index = (iteration ?: 0) % snacks.size
    snacks[index].click()

    // 항목을 클릭하고 세부정보 화면이 로드될 때까지 기다립니다. 스낵 목록이 더 이상 화면에 표시되지 않는다는 사실을 활용할 수 있습니다.
    device.wait(Until.gone(By.res("snack_list")), 5_000)
}