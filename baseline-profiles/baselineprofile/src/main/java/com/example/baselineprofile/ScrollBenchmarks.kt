package com.example.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollCompilationNone() = benchmarkScroll(CompilationMode.None())

    @Test
    fun scrollCompilationBaselineProfiles() =
        benchmarkScroll(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun benchmarkScroll(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = "com.example.baselineprofiles_codelab",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.WARM,
            iterations = 10,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                waitForAsyncContent()
            },
            measureBlock = {
                scrollSnackListJourney()
            }
        )
    }
}