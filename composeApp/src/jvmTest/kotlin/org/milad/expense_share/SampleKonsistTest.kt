package org.milad.expense_share

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import io.kotest.core.spec.style.StringSpec

class SampleKonsistTest : StringSpec({

    "classes in commonMain should not import android or java io packages" {
        Konsist
            .scopeFromProject()
            .files
            .filter { it.path.contains("src/commonMain") }
            .assertFalse { file ->
                file.hasImport { it.name.startsWith("android.") || it.name.startsWith("java.io.") }
            }
    }
})