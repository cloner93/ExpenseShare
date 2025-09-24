package kotest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SimpleTest : StringSpec({

    "1 + 1 should equal 2" {
        val sum = 1 + 1
        sum shouldBe 2
    }

    "string length should be correct" {
        val text = "Kotest"
        text.length shouldBe 6
    }
})