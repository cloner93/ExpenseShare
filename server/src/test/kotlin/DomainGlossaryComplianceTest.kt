import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * Deterministic automated check enforcing GLOSSARY.md domain language rules.
 * Fails if banned vocabulary (e.g. "chatbot", "copilot", "دستیار هوشمند") is introduced.
 */
class DomainGlossaryComplianceTest : DescribeSpec({

    describe("Domain Glossary Compliance") {
        it("strictly enforces prohibited vocabulary across AI Agent modules") {
            val rootDir = File(".").canonicalFile.parentFile ?: File(".")
            val targetDirs = listOf(
                File("src/main/kotlin/org/milad/expense_share/ai"),
                File("../composeApp/src/commonMain/kotlin/org/milad/expense_share/ai")
            )

            val bannedTerms = listOf(
                "chatbot",
                "copilot",
                "دستیار هوشمند"
            )

            val violations = mutableListOf<String>()

            targetDirs.forEach { dir ->
                if (dir.exists()) {
                    dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
                        val lines = file.readLines()
                        lines.forEachIndexed { lineIdx, line ->
                            val lower = line.lowercase()
                            bannedTerms.forEach { banned ->
                                if (lower.contains(banned)) {
                                    violations.add("${file.name}:${lineIdx + 1} contains banned term '$banned'")
                                }
                            }
                        }
                    }
                }
            }

            violations shouldBe emptyList()
        }
    }
})
