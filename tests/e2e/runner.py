#!/usr/bin/env python3
"""
Comprehensive E2E Test Suite Runner for ExpenseShare v1.0.
Executes Tier 1 (Feature Coverage), Tier 2 (Boundaries), Tier 3 (Cross-Feature Combinations),
and Tier 4 (Real-World Application Scenarios).
Generates console summary, JUnit XML, and JSON test reports.
"""

import sys
import os
import time
import argparse
import inspect
import traceback
from pathlib import Path
from typing import List, Dict, Tuple, Any

# Ensure tests/e2e is on PYTHONPATH
E2E_ROOT = Path(__file__).resolve().parent
REPO_ROOT = E2E_ROOT.parent.parent
sys.path.insert(0, str(REPO_ROOT))

# Color codes for terminal output
GREEN = "\033[92m"
RED = "\033[91m"
YELLOW = "\033[93m"
CYAN = "\033[96m"
BOLD = "\033[1m"
RESET = "\033[0m"


class TestResult:
    def __init__(self, name: str, tier: str, module: str, status: str, duration_ms: float, error: str = ""):
        self.name = name
        self.tier = tier
        self.module = module
        self.status = status
        self.duration_ms = duration_ms
        self.error = error


class E2ETestRunner:
    def __init__(self, tiers: List[str] = None, feature_filter: str = None, verbose: bool = False):
        self.tiers = tiers or ["1", "2", "3", "4"]
        if feature_filter:
            self.feature_filters = [f.strip().lower() for f in feature_filter.split(",") if f.strip()]
        else:
            self.feature_filters = []
        self.verbose = verbose
        self.results: List[TestResult] = []

    def discover_tests(self) -> List[Tuple[str, str, Any]]:
        tests = []

        tier_folders = {
            "1": ("Tier 1: Feature Coverage", E2E_ROOT / "tier1_features"),
            "2": ("Tier 2: Boundary & Corner Cases", E2E_ROOT / "tier2_boundaries"),
            "3": ("Tier 3: Cross-Feature Combinations", E2E_ROOT / "tier3_combinations"),
            "4": ("Tier 4: Real-World Application Scenarios", E2E_ROOT / "tier4_scenarios"),
        }

        for tier_key, (tier_label, folder) in tier_folders.items():
            if tier_key not in self.tiers:
                continue
            if not folder.exists():
                continue

            for py_file in sorted(folder.glob("test_*.py")):
                mod_name = f"tests.e2e.{folder.name}.{py_file.stem}"
                try:
                    import importlib
                    module = importlib.import_module(mod_name)
                    # Reload in case of changes
                    importlib.reload(module)

                    for attr_name in dir(module):
                        if attr_name.startswith("test_"):
                            func = getattr(module, attr_name)
                            if callable(func):
                                if self.feature_filters and not any(f in attr_name.lower() for f in self.feature_filters):
                                    continue
                                tests.append((tier_label, py_file.stem, func))
                except Exception as e:
                    print(f"{RED}Error loading module {mod_name}: {e}{RESET}")
                    traceback.print_exc()

        return tests

    def run(self) -> bool:
        start_time = time.time()
        tests = self.discover_tests()

        print(f"\n{BOLD}{CYAN}======================================================================{RESET}")
        print(f"{BOLD}{CYAN}   ExpenseShare v1.0 — Comprehensive E2E Test Suite Execution         {RESET}")
        print(f"{BOLD}{CYAN}======================================================================{RESET}")
        print(f"Discovered {BOLD}{len(tests)}{RESET} test cases across requested tiers: {', '.join(self.tiers)}\n")

        current_tier = ""
        passed = 0
        failed = 0

        for tier_label, mod_name, test_func in tests:
            if tier_label != current_tier:
                current_tier = tier_label
                print(f"\n{BOLD}── {current_tier} ──────────────────────────────────────{RESET}")

            t0 = time.time()
            test_name = test_func.__name__
            status = "PASS"
            err_msg = ""

            try:
                test_func()
                passed += 1
            except Exception as e:
                status = "FAIL"
                failed += 1
                err_msg = f"{type(e).__name__}: {e}"

            elapsed_ms = (time.time() - t0) * 1000
            self.results.append(TestResult(test_name, current_tier, mod_name, status, elapsed_ms, err_msg))

            if status == "PASS":
                if self.verbose:
                    print(f"  {GREEN}✓{RESET} {test_name:<60} {CYAN}{elapsed_ms:6.2f}ms{RESET}")
                else:
                    sys.stdout.write(f"{GREEN}.{RESET}")
                    sys.stdout.flush()
            else:
                if not self.verbose:
                    print()
                print(f"  {RED}✗{RESET} {test_name:<60} {RED}FAILED{RESET} ({elapsed_ms:6.2f}ms)")
                print(f"     {RED}↳ {err_msg}{RESET}")

        if not self.verbose:
            print("\n")

        total_time = time.time() - start_time
        self.print_summary(passed, failed, total_time)
        return failed == 0

    def print_summary(self, passed: int, failed: int, total_time: float):
        total = passed + failed
        print(f"\n{BOLD}{CYAN}======================================================================{RESET}")
        print(f"{BOLD}{CYAN}   E2E Test Execution Summary                                         {RESET}")
        print(f"{BOLD}{CYAN}======================================================================{RESET}")
        
        # Breakdown by Tier
        tier_counts: Dict[str, Dict[str, int]] = {}
        for r in self.results:
            if r.tier not in tier_counts:
                tier_counts[r.tier] = {"total": 0, "pass": 0, "fail": 0}
            tier_counts[r.tier]["total"] += 1
            if r.status == "PASS":
                tier_counts[r.tier]["pass"] += 1
            else:
                tier_counts[r.tier]["fail"] += 1

        print(f"{'Tier':<45} | {'Total':<6} | {'Passed':<6} | {'Failed':<6}")
        print("─" * 70)
        for tier, counts in tier_counts.items():
            color = GREEN if counts["fail"] == 0 else RED
            print(f"{tier:<45} | {counts['total']:<6} | {GREEN}{counts['pass']:<6}{RESET} | {color}{counts['fail']:<6}{RESET}")
        print("─" * 70)

        status_color = GREEN if failed == 0 else RED
        status_text = "ALL TESTS PASSED" if failed == 0 else "FAILURES DETECTED"
        print(f"{BOLD}Status: {status_color}{status_text}{RESET}")
        print(f"Total Tests: {BOLD}{total}{RESET} | Passed: {GREEN}{passed}{RESET} | Failed: {status_color}{failed}{RESET}")
        print(f"Execution Duration: {CYAN}{total_time:.2f} seconds{RESET}")
        print(f"{BOLD}{CYAN}======================================================================{RESET}\n")

    def export_junit_xml(self, xml_path: Path):
        xml_path.parent.mkdir(parents=True, exist_ok=True)
        total = len(self.results)
        failures = sum(1 for r in self.results if r.status == "FAIL")
        total_time = sum(r.duration_ms for r in self.results) / 1000.0

        lines = [
            '<?xml version="1.0" encoding="UTF-8"?>',
            f'<testsuite name="ExpenseShare_E2E" tests="{total}" failures="{failures}" errors="0" time="{total_time:.3f}">'
        ]
        for r in self.results:
            lines.append(f'  <testcase classname="{r.tier}.{r.module}" name="{r.name}" time="{r.duration_ms / 1000.0:.3f}">')
            if r.status == "FAIL":
                lines.append(f'    <failure message="{r.error}">')
                lines.append(f'      {r.error}')
                lines.append('    </failure>')
            lines.append('  </testcase>')
        lines.append('</testsuite>')

        xml_path.write_text("\n".join(lines), encoding="utf-8")
        print(f"Exported JUnit XML report to: {xml_path}")


def main():
    parser = argparse.ArgumentParser(description="ExpenseShare v1.0 E2E Test Suite Runner")
    parser.add_argument("--tier", type=str, default="all", help="Tiers to run (e.g. 1, 2, 3, 4 or 'all')")
    parser.add_argument("--feature", type=str, default="", help="Filter by feature (e.g. f1, f5, settlement)")
    parser.add_argument("--xml-report", type=str, default="", help="Path to write JUnit XML report")
    parser.add_argument("-v", "--verbose", action="store_true", help="Verbose test progress")

    args = parser.parse_args()

    if args.tier.lower() == "all":
        selected_tiers = ["1", "2", "3", "4"]
    else:
        selected_tiers = [t.strip() for t in args.tier.split(",")]

    runner = E2ETestRunner(tiers=selected_tiers, feature_filter=args.feature, verbose=args.verbose)
    success = runner.run()

    if args.xml_report:
        runner.export_junit_xml(Path(args.xml_report).resolve())

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
