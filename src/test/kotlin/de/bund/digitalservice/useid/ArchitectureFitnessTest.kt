package de.bund.digitalservice.useid

import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

@AnalyzeClasses(packages = ["de.bund.digitalservice"])
class ArchitectureFitnessTest {

    @ArchTest
    fun `prevent package import cycles`() {
        slices().matching("de.bund.digitalservice.(**)").should().beFreeOfCycles()
    }
}
