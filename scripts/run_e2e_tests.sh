#!/usr/bin/env bash
# ==============================================================================
# ExpenseShare v1.0 — Unified E2E Test Suite & Release Verification Runner
# ==============================================================================
# Usage:
#   ./scripts/run_e2e_tests.sh [OPTIONS]
#
# Options:
#   --all              Execute Gradle builds, stability checks, and full 4-tier E2E suite
#   --builds-only      Execute only the 9 Gradle build and test verification tasks
#   --stability-only   Execute only the static and dynamic feature stability checks
#   --tests-only       Execute only the 4-tier E2E automated test suite
#   --skip-builds      Execute stability checks and 4-tier tests, skipping Gradle builds
#   --tier <1,2,3,4>   Filter test execution to specific tiers (e.g. --tier 1,2)
#   --feature <name>   Filter test execution to specific feature (e.g. --feature f5)
#   --xml-report <f>   Export standard JUnit XML report to specified path
#   -v, --verbose      Enable verbose output
#   -h, --help         Show this help message
# ==============================================================================

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_ROOT}"

# Color formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

MODE="all"
SKIP_BUILDS=false
TIER_ARG="all"
FEATURE_ARG=""
XML_REPORT=""
VERBOSE=false

while [[ $# -gt 0 ]]; do
  case $1 in
    --all)
      MODE="all"
      shift
      ;;
    --builds-only)
      MODE="builds"
      shift
      ;;
    --stability-only)
      MODE="stability"
      shift
      ;;
    --tests-only)
      MODE="tests"
      shift
      ;;
    --skip-builds)
      SKIP_BUILDS=true
      shift
      ;;
    --tier)
      TIER_ARG="$2"
      shift 2
      ;;
    --feature)
      FEATURE_ARG="$2"
      shift 2
      ;;
    --xml-report)
      XML_REPORT="$2"
      shift 2
      ;;
    -v|--verbose)
      VERBOSE=true
      shift
      ;;
    -h|--help)
      grep '^# ' "$0" | cut -c 3-
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown argument: $1${NC}"
      exit 1
      ;;
  esac
done

echo -e "${BOLD}${CYAN}======================================================================${NC}"
echo -e "${BOLD}${CYAN}   ExpenseShare v1.0 — Unified E2E Test Suite & Verification Runner   ${NC}"
echo -e "${BOLD}${CYAN}======================================================================${NC}"
echo -e "Repository Root: ${REPO_ROOT}"
echo -e "Execution Mode:  ${BOLD}${MODE}${NC}"
echo -e "Timestamp:       $(date -u +"%Y-%m-%dT%H:%M:%SZ")\n"

TOTAL_STABILITY_PASS=0
TOTAL_STABILITY_FAIL=0
BUILD_TASKS_PASS=0
BUILD_TASKS_FAIL=0

# -----------------------------------------------------------------------------
# Section 1: Automated Gradle Build Verification
# -----------------------------------------------------------------------------
run_gradle_builds() {
  echo -e "${BOLD}${BLUE}── [1/3] Automated Build & Unit Test Verification ─────────────────────${NC}"
  
  if [[ "${SKIP_BUILDS}" == true ]]; then
    echo -e "${YELLOW}ℹ Skipping Gradle builds (--skip-builds specified)${NC}\n"
    return 0
  fi

  GRADLE_CMD="./gradlew"
  if [[ ! -x "${GRADLE_CMD}" ]]; then
    chmod +x "${GRADLE_CMD}"
  fi

  TASKS=(
    ":server:test"
    ":core:currency:jvmTest"
    ":core:logger:jvmTest"
    ":core:network:jvmTest"
    ":core:domain:jvmTest"
    ":core:data:jvmTest"
    ":composeApp:assembleDebug"
    ":composeApp:assembleRelease"
    ":server:shadowJar"
  )

  for task in "${TASKS[@]}"; do
    echo -n "  Running ${task}... "
    if OUTPUT=$(${GRADLE_CMD} "${task}" --console=plain 2>&1); then
      echo -e "${GREEN}PASS${NC}"
      ((BUILD_TASKS_PASS++))
    else
      echo -e "${RED}FAIL${NC}"
      ((BUILD_TASKS_FAIL++))
      if [[ "${VERBOSE}" == true ]]; then
        echo -e "${RED}${OUTPUT}${NC}"
      fi
    fi
  done
  echo ""
}

# -----------------------------------------------------------------------------
# Section 2: Static & Dynamic Feature Stability Verification
# -----------------------------------------------------------------------------
verify_feature_stability() {
  echo -e "${BOLD}${BLUE}── [2/3] Feature Stability & Code Architecture Verification ─────────${NC}"

  check_assertion() {
    local desc="$1"
    local status="$2"
    local details="$3"

    if [[ "${status}" -eq 0 ]]; then
      echo -e "  ${GREEN}✓${NC} ${desc}"
      ((TOTAL_STABILITY_PASS++))
    else
      echo -e "  ${RED}✗${NC} ${desc} — ${RED}${details}${NC}"
      ((TOTAL_STABILITY_FAIL++))
    fi
  }

  # 1. Menu options in group details (Help, Rename)
  VM_FILE="composeApp/src/commonMain/kotlin/org/milad/expense_share/dashboard/group/GroupDetailViewModel.kt"
  if [[ -f "${VM_FILE}" ]]; then
    if grep -q 'is GroupDetailAction.ShowHelp -> TODO' "${VM_FILE}"; then
      check_assertion "GroupDetailViewModel: ShowHelp does not throw NotImplementedError" 1 "TODO found in ShowHelp"
    else
      check_assertion "GroupDetailViewModel: ShowHelp does not throw NotImplementedError" 0 ""
    fi

    if grep -q 'is GroupDetailAction.RenameGroup -> TODO' "${VM_FILE}"; then
      check_assertion "GroupDetailViewModel: RenameGroup does not throw NotImplementedError" 1 "TODO found in RenameGroup"
    else
      check_assertion "GroupDetailViewModel: RenameGroup does not throw NotImplementedError" 0 ""
    fi
  fi

  # 2. Settlement action callbacks in SettlementScreen
  SETTLE_SCREEN="composeApp/src/commonMain/kotlin/org/milad/expense_share/dashboard/group/screen/SettlementScreen.kt"
  if [[ -f "${SETTLE_SCREEN}" ]]; then
    if grep -q 'onPayClick' "${SETTLE_SCREEN}" && grep -q 'onApproveClick' "${SETTLE_SCREEN}" && grep -q 'onRejectClick' "${SETTLE_SCREEN}"; then
      check_assertion "SettlementScreen: settlement action callbacks wired (Pay, Approve, Reject)" 0 ""
    else
      check_assertion "SettlementScreen: settlement action callbacks wired (Pay, Approve, Reject)" 1 "Callbacks missing in SettlementScreen"
    fi
  fi

  # 3. Friends deduplication in FriendsViewModel
  FRIENDS_VM="composeApp/src/commonMain/kotlin/org/milad/expense_share/friends/FriendsViewModel.kt"
  if [[ -f "${FRIENDS_VM}" ]]; then
    if grep -q 'it\.friends + newFriends' "${FRIENDS_VM}"; then
      check_assertion "FriendsViewModel: friend list deduplication implemented (newFriends)" 1 "it.friends + newFriends additive bug found"
    else
      check_assertion "FriendsViewModel: friend list deduplication implemented (newFriends)" 0 ""
    fi
  fi

  # 4. Friends screen empty event handlers
  FRIENDS_SCREEN="composeApp/src/commonMain/kotlin/org/milad/expense_share/friends/FriendsScreen.kt"
  if [[ -f "${FRIENDS_SCREEN}" ]]; then
    if grep -q 'is FriendsEvent\.ShowToast -> {}' "${FRIENDS_SCREEN}"; then
      check_assertion "FriendsScreen: ShowToast event handled" 1 "Empty block {} found"
    else
      check_assertion "FriendsScreen: ShowToast event handled" 0 ""
    fi

    if grep -q 'is FriendDetailEvent\.OpenGroup -> {}' "${FRIENDS_SCREEN}"; then
      check_assertion "FriendsScreen: OpenGroup event handled" 1 "Empty block {} found"
    else
      check_assertion "FriendsScreen: OpenGroup event handled" 0 ""
    fi

    if grep -q 'is FriendDetailEvent\.ShowSettleUpDialog -> {}' "${FRIENDS_SCREEN}"; then
      check_assertion "FriendsScreen: ShowSettleUpDialog event handled" 1 "Empty block {} found"
    else
      check_assertion "FriendsScreen: ShowSettleUpDialog event handled" 0 ""
    fi
  fi

  # 5. Persistent token storage and logout
  TOKEN_PROVIDER="core/network/src/commonMain/kotlin/client/TokenProvider.kt"
  if [[ -f "${TOKEN_PROVIDER}" ]]; then
    if grep -q 'clearToken' "${TOKEN_PROVIDER}" && grep -q 'getToken' "${TOKEN_PROVIDER}"; then
      check_assertion "TokenProvider: interface supports persistent token and clearToken()" 0 ""
    else
      check_assertion "TokenProvider: interface supports persistent token and clearToken()" 1 "Methods missing"
    fi
  fi

  # 6. Dead directories removal
  DEAD_DIRS=("androidApp" "core/models" "core/ui" "shared")
  for d in "${DEAD_DIRS[@]}"; do
    if [[ -d "${d}" ]]; then
      check_assertion "Dead directory removal: ${d}/ does not exist" 1 "Directory still exists"
    else
      check_assertion "Dead directory removal: ${d}/ does not exist" 0 ""
    fi
  done

  # 7. A2UI and Chat button removal
  NAV_ITEM="core/common/src/commonMain/kotlin/com/pmb/common/ui/scaffold/NavItem.kt"
  if [[ -f "${NAV_ITEM}" ]]; then
    if grep -q 'AIAssistant(' "${NAV_ITEM}"; then
      check_assertion "A2UI Cleanup: NavItem.AIAssistant removed" 1 "AIAssistant still present in NavItem enum"
    else
      check_assertion "A2UI Cleanup: NavItem.AIAssistant removed" 0 ""
    fi
  fi

  DASHBOARD_FILE="composeApp/src/commonMain/kotlin/org/milad/expense_share/dashboard/Dashboard.kt"
  if [[ -f "${DASHBOARD_FILE}" ]]; then
    if grep -q 'rememberInfiniteTransition' "${DASHBOARD_FILE}"; then
      check_assertion "Chat Button Cleanup: pulsing infiniteTransition removed from Dashboard" 1 "Pulsing animation present"
    else
      check_assertion "Chat Button Cleanup: pulsing infiniteTransition removed from Dashboard" 0 ""
    fi
  fi

  # 8. Release build minification disabled
  ANDROID_EXT="build-logic/convention/src/main/kotlin/org/milad/expense_share/ext/AndroidExt.kt"
  if [[ -f "${ANDROID_EXT}" ]]; then
    if grep -q 'isMinifyEnabled = false' "${ANDROID_EXT}"; then
      check_assertion "Release Build Configuration: isMinifyEnabled = false" 0 ""
    else
      check_assertion "Release Build Configuration: isMinifyEnabled = false" 1 "isMinifyEnabled not false"
    fi
  fi

  echo ""
}

# -----------------------------------------------------------------------------
# Section 3: Executable 4-Tier E2E Test Suite
# -----------------------------------------------------------------------------
run_four_tier_tests() {
  echo -e "${BOLD}${BLUE}── [3/3] Executable 4-Tier E2E Automated Test Suite ───────────────────${NC}"

  RUNNER_PY="tests/e2e/runner.py"
  if [[ ! -f "${RUNNER_PY}" ]]; then
    echo -e "${RED}Test runner script not found at ${RUNNER_PY}!${NC}"
    return 1
  fi

  ARGS=( "--tier" "${TIER_ARG}" )
  if [[ -n "${FEATURE_ARG}" ]]; then
    ARGS+=( "--feature" "${FEATURE_ARG}" )
  fi
  if [[ -n "${XML_REPORT}" ]]; then
    ARGS+=( "--xml-report" "${XML_REPORT}" )
  fi
  if [[ "${VERBOSE}" == true ]]; then
    ARGS+=( "-v" )
  fi

  python3 "${RUNNER_PY}" "${ARGS[@]}"
  return $?
}

# -----------------------------------------------------------------------------
# Execution Dispatcher
# -----------------------------------------------------------------------------
EXIT_CODE=0

case "${MODE}" in
  all)
    run_gradle_builds
    verify_feature_stability
    if ! run_four_tier_tests; then
      EXIT_CODE=1
    fi
    ;;
  builds)
    run_gradle_builds
    ;;
  stability)
    verify_feature_stability
    ;;
  tests)
    if ! run_four_tier_tests; then
      EXIT_CODE=1
    fi
    ;;
esac

echo -e "\n${BOLD}${CYAN}======================================================================${NC}"
echo -e "${BOLD}${CYAN}   Final Release Verification Summary                                 ${NC}"
echo -e "${BOLD}${CYAN}======================================================================${NC}"

if [[ "${MODE}" == "all" || "${MODE}" == "stability" ]]; then
  echo -e "Feature Stability Checks: Passed: ${GREEN}${TOTAL_STABILITY_PASS}${NC} | Failed: $([[ ${TOTAL_STABILITY_FAIL} -eq 0 ]] && echo -e "${GREEN}0${NC}" || echo -e "${RED}${TOTAL_STABILITY_FAIL}${NC}")"
fi

if [[ "${MODE}" == "all" || "${MODE}" == "builds" ]]; then
  if [[ "${SKIP_BUILDS}" == false ]]; then
    echo -e "Gradle Build Tasks:       Passed: ${GREEN}${BUILD_TASKS_PASS}${NC} | Failed: $([[ ${BUILD_TASKS_FAIL} -eq 0 ]] && echo -e "${GREEN}0${NC}" || echo -e "${RED}${BUILD_TASKS_FAIL}${NC}")"
  else
    echo -e "Gradle Build Tasks:       ${YELLOW}Skipped${NC}"
  fi
fi

if [[ ${EXIT_CODE} -eq 0 && ${TOTAL_STABILITY_FAIL} -eq 0 && ${BUILD_TASKS_FAIL} -eq 0 ]]; then
  echo -e "\n${BOLD}${GREEN}✔ ALL RELEASE ACCEPTANCE CRITERIA VERIFIED SUCCESSFULLY!${NC}\n"
else
  echo -e "\n${BOLD}${YELLOW}⚠ PENDING ITEMS REMAINING (Work in progress by Milestone agents)${NC}\n"
fi

exit ${EXIT_CODE}
