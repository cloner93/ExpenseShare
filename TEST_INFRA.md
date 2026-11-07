# ExpenseShare v1.0 — Test Infrastructure & E2E Testing Architecture

## 1. Testing Philosophy & Methodology

The ExpenseShare v1.0 test suite is designed under the **Dual Track E2E Testing Methodology**. This framework operates on two synchronized tracks:
1. **Implementation Track**: Progresses across milestones (M1 Server Test Suite, M2 Core Modules & Platform Storage, M3 UI Cleanup & Feature Fixes).
2. **E2E Testing Track**: Develops requirement-driven, opaque-box tests derived strictly from user requirements and acceptance criteria in `ORIGINAL_REQUEST.md` and `PROJECT.md`.

### Core Tenets
- **Requirement-Driven & Opaque-Box**: Tests are derived from observable behavior, interface contracts, and acceptance criteria rather than matching arbitrary internal implementation details.
- **Progressive Verifiability**: Tests can execute at any point in the development lifecycle. Incomplete features fail deterministically with actionable assertion messages; completed features pass cleanly.
- **Zero-Pollution & Scope Boundary**: Test code and test scripts reside strictly in `tests/e2e/` and `scripts/`, never mutating product implementation code in `core/`, `server/src/main/`, or `composeApp/src/main/`.

---

## 2. Feature Inventory & Acceptance Criteria Matrix

| #  | Feature ID                  | Target Area                 | Description                                                                                   | Milestone | Acceptance Criteria Verified                                                |
|----|-----------------------------|-----------------------------|-----------------------------------------------------------------------------------------------|-----------|-----------------------------------------------------------------------------|
| 1  | `F1_A2UI_CLEANUP`           | `core:common`, `composeApp` | Remove experimental A2UI (`NavItem.AIAssistant`) from navigation                              | M3        | Navigation across all screen sizes has 3 tabs; no AIAssistant route         |
| 2  | `F2_CHAT_BUTTON_CLEANUP`    | `composeApp`                | Remove pulsing chat button (`rememberInfiniteTransition`, `ModalBottomSheet`)                 | M3        | Dashboard TopAppBar has no pulsing chat button; ChatScreen unreachable      |
| 3  | `F3_DEAD_DIRS_CLEANUP`      | Project Root                | Delete unused directories (`androidApp/`, `core/models/`, `core/ui/`, `shared/`)              | M3        | All 4 directories removed; settings.gradle.kts includes only active modules |
| 4  | `F4_GROUP_CRASH_FIX`        | `composeApp`                | Safely handle `ShowHelp` and remove/disable `RenameGroup` without `NotImplementedError`       | M3        | Group menu options never throw NotImplementedError or crash app             |
| 5  | `F5_SETTLEMENT_WIRING`      | `composeApp`, `core:domain` | Wire `SettlementScreen` callbacks (`onPayClick`, etc.) and inject use cases into VM           | M3        | Pay, Confirm, Dispute buttons trigger domain use cases and reload state     |
| 6  | `F6_FRIENDS_DEDUPLICATION`  | `composeApp`                | Fix friend list duplication in `FriendsViewModel` (`it.friends + newFriends` -> `newFriends`) | M3        | Navigating to or refreshing Friends screen preserves unique friends         |
| 7  | `F7_FRIENDS_EMPTY_HANDLERS` | `composeApp`                | Implement empty handlers in `FriendsScreen` (ShowToast, OpenGroup, SettleUpDialog)            | M3        | UI events trigger toast, group navigation, and settle-up dialog             |
| 8  | `F8_AUTH_TOKEN_PERSISTENCE` | `core:network`, `core:data` | Store tokens persistently (SharedPreferences/Preferences); clear token on logout              | M2        | User session persists across app restart; logout purges tokens              |
| 9  | `F9_SERVER_TEST_FIX`        | `server`                    | Add `io.mockk:mockk` dependency; fix `SettlementStatus` import in `SettlementServiceTest`     | M1        | `./gradlew :server:test` passes with 0 failures                             |
| 10 | `F10_CORE_NETWORK_TEST_FIX` | `core:network`              | Use top-level `safeNetworkCall`; use MockEngine in `ApiClientTest`                            | M2        | `./gradlew :core:network:jvmTest` passes with 0 failures                    |
| 11 | `F11_CORE_DOMAIN_TEST_FIX`  | `core:domain`               | Update `Transaction` test constructors with `Amount`, payers, shares; test fakes              | M2        | `./gradlew :core:domain:jvmTest` passes with 0 failures                     |
| 12 | `F12_CORE_DATA_TEST_FIX`    | `core:data`                 | Add Kotest test dependencies; fix constructor args & `,,,` syntax error                       | M2        | `./gradlew :core:data:jvmTest` passes with 0 failures                       |
| 13 | `F13_RELEASE_BASE_URL`      | `core:network`              | Configure platform-aware Base URL (`10.0.2.2:8082` Android, `127.0.0.1:8082` JVM)             | M2        | Platform actuals provide correct host; configurable via Koin for prod       |
| 14 | `F14_RELEASE_BUILD_CONFIG`  | Build Logic                 | Keep minification disabled (`isMinifyEnabled = false`); assembleDebug/Release/shadowJar       | M3        | Release build minification disabled; all 3 release packages compile cleanly |
| 15 | `F15_E2E_VERIFICATION`      | Full Suite                  | Full application verification passing 100% of acceptance criteria and E2E suite               | M4        | 170 E2E tests passing; 9 automated builds passing; stability verified       |

---

## 3. Four-Tier Test Suite Architecture

The test suite contains **170 automated test cases** organized into 4 hierarchical tiers:

```
tests/e2e/
├── common.py                       # Domain simulation models, algorithms & assertions
├── runner.py                       # CLI test runner with JUnit XML & TAP export
├── tier1_features/                 # Tier 1: Feature Coverage (75 tests, >=5 per feature)
│   ├── test_tier1_ui_cleanup.py    # F1 (5 tests), F2 (5 tests), F3 (5 tests)
│   ├── test_tier1_features_r2.py   # F4 (5 tests), F5 (5 tests), F6 (5 tests), F7 (5 tests), F8 (5 tests)
│   └── test_tier1_tests_r3_r4.py   # F9 (5 tests), F10 (5 tests), F11 (5 tests), F12 (5 tests), F13 (5 tests), F14 (5 tests), F15 (5 tests)
├── tier2_boundaries/               # Tier 2: Boundary & Corner Cases (75 tests, >=5 per feature)
│   ├── test_tier2_ui_cleanup_boundaries.py    # F1-F3 boundaries (15 tests)
│   ├── test_tier2_features_r2_boundaries.py   # F4-F8 boundaries (25 tests)
│   └── test_tier2_tests_r3_r4_boundaries.py   # F9-F15 boundaries (35 tests)
├── tier3_combinations/             # Tier 3: Cross-Feature Combinations (15 tests)
│   └── test_cross_feature_combinations.py     # Pairwise & multi-module integration
└── tier4_scenarios/                # Tier 4: Real-World Application Scenarios (5 scenarios)
    ├── test_scenario_1_vacation_trip.py
    ├── test_scenario_2_roommates_monthly_bills.py
    ├── test_scenario_3_dinner_with_new_friends.py
    ├── test_scenario_4_session_recovery_and_settle.py
    └── test_scenario_5_multi_currency_and_extreme_amounts.py
```

### Tier Breakdown
- **Tier 1 (Feature Coverage — 75 tests)**: Evaluates each feature (F1 through F15) in isolation, checking contract adherence, expected function signatures, UI layout declarations, and AST patterns.
- **Tier 2 (Boundary & Corner Cases — 75 tests)**: Stress-tests each feature against extreme values (64-bit Long max), negative numbers, 10,000 items, rapid click bursts, process death/restart, network timeouts, empty payloads, and unauthorized access attempts.
- **Tier 3 (Cross-Feature Combinations — 15 tests)**: Validates pairwise and multi-feature interaction across Auth, Groups, Transactions, Settlements, Friends, Token Storage, and Navigation.
- **Tier 4 (Real-World Application Scenarios — 5 scenarios)**: Simulates complex, end-to-end multi-user workflows from start to finish.

---

## 4. Tier 4 Real-World Application Scenarios

### Scenario 1: Multi-User Vacation Trip ("Trip to Istanbul")
- **Workflow**: 4 participants (Milad, Ali, Sara, Reza) on a group trip.
- **Expenses**:
  1. Flights: 900,000 paid by Milad, split equally (300,000 each: Milad, Sara, Reza).
  2. Hotel: 450,000 paid by Sara, split equally (150,000 each: Milad, Sara, Reza).
  3. Sightseeing: 180,000 paid by Reza, split equally (60,000 each: Milad, Sara, Reza).
- **Settlement Matrix**:
  - Milad Net: `+900,000 - 510,000 = +390,000` (Creditor)
  - Sara Net: `+450,000 - 510,000 = -60,000` (Debtor)
  - Reza Net: `+180,000 - 510,000 = -330,000` (Debtor)
  - Ali Net: `0`
- **Resolution**: Reza pays Milad 330,000 (confirmed); Sara pays Milad 60,000 (confirmed); final group net balance is strictly zero.

### Scenario 2: Roommates Monthly Shared Bills & Dispute Handling
- **Workflow**: Alice, Bob, and Charlie sharing an apartment.
- **Initial Bills**: Rent ($1,800 paid by Alice), Internet ($90 paid by Bob), Groceries ($150 paid by Charlie).
- **Dispute Event**: Bob marks $590 as paid. Alice disputes because an electricity bill was omitted. Status reverts from `PAID` to `PENDING`.
- **Amendment & Recalculation**: Alice adds Electricity ($60 paid by Alice, $20 each). Bob pays updated $610 (confirmed); Charlie pays $550 (confirmed).

### Scenario 3: Dinner & Settle-Up with New Friends
- **Workflow**: Dave connects with new coworkers Emma, Frank, and Grace.
- **Deduplication Check**: Accepts friend requests; refreshes 5 times to confirm no duplicate friend entries accumulate.
- **Group Dinner**: Dave pays $140 for dinner ($35 each).
- **Direct Settle-Up**: Frank triggers the Settle-Up dialog directly from `FriendsScreen`, settles his $35 debt, and Dave confirms.

### Scenario 4: Offline / Restart Session Recovery & Settlement Flow
- **Workflow**: Authenticated user session persistence across cold app restarts.
- **Execution**: User logs in -> Tokens stored in persistent storage -> Process killed -> Cold restart -> Tokens restored -> App opens directly into Dashboard -> Navigates to GroupDetail (menu options Help and Rename handled without crash) -> Settles pending balance -> Logs out -> Tokens purged -> Navigates to Auth screen.

### Scenario 5: Multi-Currency & Extreme Amount Settlement Flow
- **Workflow**: Global conference trip with multi-million / billion Rial amounts.
- **Precision**: Verifies 64-bit integer Amount types prevent integer overflow on large values (50,000,000 Rials).
- **Unequal Splitting**: 3-way split of 10,000,000 produces exact penny-perfect allocations (3,333,334, 3,333,333, 3,333,333) with zero remainder.
- **Settlement Symmetry**: Total credit settlements strictly equal total debit settlements.

---

## 5. Coverage Metrics & Traceability Matrix

| Feature | Tier 1 Tests | Tier 2 Boundaries | Tier 3 Combinations | Tier 4 Scenarios | Total Coverage |
|---------|--------------|-------------------|---------------------|------------------|----------------|
| F1: A2UI Cleanup | 5 | 5 | 1 | 1 | 12 |
| F2: Chat Button Cleanup | 5 | 5 | 1 | 1 | 12 |
| F3: Dead Dirs Cleanup | 5 | 5 | 1 | 1 | 12 |
| F4: Group Crash Fix | 5 | 5 | 2 | 2 | 14 |
| F5: Settlement Wiring | 5 | 5 | 4 | 4 | 18 |
| F6: Friends Deduplication | 5 | 5 | 2 | 2 | 14 |
| F7: Friends Empty Handlers | 5 | 5 | 2 | 2 | 14 |
| F8: Token Persistence | 5 | 5 | 3 | 2 | 15 |
| F9: Server Test Fix | 5 | 5 | 2 | 2 | 14 |
| F10: Network Test Fix | 5 | 5 | 2 | 2 | 14 |
| F11: Domain Test Fix | 5 | 5 | 2 | 2 | 14 |
| F12: Data Test Fix | 5 | 5 | 2 | 2 | 14 |
| F13: Release Base URL | 5 | 5 | 2 | 2 | 14 |
| F14: Release BuildConfig | 5 | 5 | 1 | 1 | 12 |
| F15: E2E Verification | 5 | 5 | 3 | 3 | 16 |
| **Total Test Cases** | **75** | **75** | **15** | **5** | **170** |

---

## 6. Test Runner & Execution Guide

The unified runner is available at `scripts/run_e2e_tests.sh` and `tests/e2e/runner.py`.

### Commands
```bash
# Full release verification (Builds + Stability + All 4 Tiers)
./scripts/run_e2e_tests.sh --all

# Fast test run skipping Gradle builds (Stability + All 4 Tiers)
./scripts/run_e2e_tests.sh --skip-builds

# Run specific tier only
python3 tests/e2e/runner.py --tier 4 -v

# Run tests filtered by feature
python3 tests/e2e/runner.py --feature settlement -v

# Generate JUnit XML report for CI/CD
./scripts/run_e2e_tests.sh --skip-builds --xml-report test-results/e2e-report.xml
```
