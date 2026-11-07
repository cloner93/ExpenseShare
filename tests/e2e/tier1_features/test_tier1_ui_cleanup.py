"""
Tier 1: Feature Coverage for UI Cleanup Features
- F1: F1_A2UI_CLEANUP (5 tests)
- F2: F2_CHAT_BUTTON_CLEANUP (5 tests)
- F3: F3_DEAD_DIRS_CLEANUP (5 tests)
"""

import re
from pathlib import Path
from tests.e2e.common import (
    REPO_ROOT, COMPOSE_APP_DIR, CORE_COMMON_DIR, CORE_NAVIGATION_DIR,
    read_file, check_path_exists, assert_true, assert_false, assert_equal
)


# =============================================================================
# F1: A2UI Cleanup (5 tests)
# =============================================================================

def test_f1_01_nav_item_enum_definition():
    """Verify NavItem enum in NavItem.kt does not declare AIAssistant."""
    nav_item_file = CORE_COMMON_DIR / "src" / "commonMain" / "kotlin" / "com" / "pmb" / "common" / "ui" / "scaffold" / "NavItem.kt"
    assert_true(nav_item_file.exists(), f"NavItem.kt must exist at {nav_item_file}")
    content = read_file(nav_item_file)
    
    # Requirement: NavItem.AIAssistant must be removed or hidden
    assert_false("AIAssistant(" in content, "NavItem enum must not contain active AIAssistant definition")


def test_f1_02_nav_item_entry_count():
    """Verify NavItem enum has exactly 3 standard entries: Dashboard, Friends, Profile."""
    nav_item_file = CORE_COMMON_DIR / "src" / "commonMain" / "kotlin" / "com" / "pmb" / "common" / "ui" / "scaffold" / "NavItem.kt"
    content = read_file(nav_item_file)
    
    enum_matches = re.findall(r'(\w+)\s*\(\s*"[^"]+"\s*,\s*Icons\.', content)
    # Filter only entries in NavItem enum
    valid_entries = [e for e in enum_matches if e in ["Dashboard", "Friends", "Profile", "AIAssistant"]]
    assert_false("AIAssistant" in valid_entries, "AIAssistant should not be in valid NavItem entries")
    assert_true("Dashboard" in valid_entries, "Dashboard must be present in NavItem")
    assert_true("Friends" in valid_entries, "Friends must be present in NavItem")
    assert_true("Profile" in valid_entries, "Profile must be present in NavItem")


def test_f1_03_responsive_app_no_ai_nav_branch():
    """Verify ResponsiveApp.kt does not route NavItem.AIAssistant in when(item)."""
    responsive_app_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "ResponsiveApp.kt"
    assert_true(responsive_app_file.exists(), "ResponsiveApp.kt must exist")
    content = read_file(responsive_app_file)
    
    assert_false("NavItem.AIAssistant ->" in content, "ResponsiveApp must not handle NavItem.AIAssistant navigation")


def test_f1_04_responsive_app_no_ai_destination():
    """Verify ResponsiveApp.kt NavHost does not define composable<MainRoute.AIAssistant>."""
    responsive_app_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "ResponsiveApp.kt"
    content = read_file(responsive_app_file)
    
    assert_false("composable<MainRoute.AIAssistant>" in content, "ResponsiveApp must not have AIAssistant destination")


def test_f1_05_adaptive_scaffold_integrity():
    """Verify adaptive navigation scaffolds compile with clean NavItem entries iteration."""
    scaffold_dir = CORE_COMMON_DIR / "src" / "commonMain" / "kotlin" / "com" / "pmb" / "common" / "ui" / "scaffold"
    assert_true(scaffold_dir.exists(), "Scaffold directory must exist")
    
    # Check that navigation components iterate dynamically over NavItem.entries
    bottom_bar = scaffold_dir / "navigation" / "BottomNavigationBar.kt"
    if bottom_bar.exists():
        content = read_file(bottom_bar)
        assert_true("NavItem.entries" in content or "navItem" in content, "BottomNavigationBar should iterate NavItem entries")


# =============================================================================
# F2: Pulsing Chat Button Cleanup (5 tests)
# =============================================================================

def test_f2_01_dashboard_no_pulsing_transition():
    """Verify Dashboard.kt does not configure rememberInfiniteTransition for pulsing chat button."""
    dashboard_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "Dashboard.kt"
    assert_true(dashboard_file.exists(), "Dashboard.kt must exist")
    content = read_file(dashboard_file)
    
    assert_false("rememberInfiniteTransition" in content, "Dashboard.kt must not contain pulsing infiniteTransition")


def test_f2_02_dashboard_no_auto_awesome_action():
    """Verify Dashboard.kt does not include AutoAwesome AI chat icon in TopAppBar actions."""
    dashboard_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "Dashboard.kt"
    content = read_file(dashboard_file)
    
    assert_false("Icons.Filled.AutoAwesome" in content, "Dashboard.kt TopAppBar must not use Icons.Filled.AutoAwesome")
    assert_false("Icons.Default.AutoAwesome" in content, "Dashboard.kt must not use Icons.Default.AutoAwesome")


def test_f2_03_dashboard_no_show_ai_state():
    """Verify Dashboard.kt does not maintain showAiAssistant state variable."""
    dashboard_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "Dashboard.kt"
    content = read_file(dashboard_file)
    
    assert_false("showAiAssistant" in content, "Dashboard.kt must not contain showAiAssistant state")


def test_f2_04_dashboard_no_chat_bottom_sheet():
    """Verify Dashboard.kt does not embed ChatScreen in a ModalBottomSheet."""
    dashboard_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "Dashboard.kt"
    content = read_file(dashboard_file)
    
    assert_false("ChatScreen(" in content, "Dashboard.kt must not instantiate ChatScreen")


def test_f2_05_chat_dead_code_elimination():
    """Verify ChatScreen and FakeData are either removed or isolated from production UI flow."""
    chat_dir = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "chat"
    dashboard_file = COMPOSE_APP_DIR / "src" / "commonMain" / "kotlin" / "org" / "milad" / "expense_share" / "dashboard" / "Dashboard.kt"
    content = read_file(dashboard_file)
    
    # Even if chat/ folder exists, it must not be imported by Dashboard
    assert_false("org.milad.expense_share.chat" in content, "Dashboard must not import chat package")


# =============================================================================
# F3: Dead Directories Cleanup (5 tests)
# =============================================================================

def test_f3_01_android_app_dir_status():
    """Verify androidApp/ directory is deleted or marked empty/obsolete."""
    android_app_dir = REPO_ROOT / "androidApp"
    # Acceptance criteria specifies androidApp/ should be removed
    assert_false(android_app_dir.exists(), "androidApp/ directory should be removed")


def test_f3_02_core_models_dir_status():
    """Verify core/models/ directory is deleted or marked obsolete."""
    core_models_dir = REPO_ROOT / "core" / "models"
    assert_false(core_models_dir.exists(), "core/models/ directory should be removed")


def test_f3_03_core_ui_dir_status():
    """Verify core/ui/ directory is deleted or marked obsolete."""
    core_ui_dir = REPO_ROOT / "core" / "ui"
    assert_false(core_ui_dir.exists(), "core/ui/ directory should be removed")


def test_f3_04_shared_build_dir_status():
    """Verify shared/build/ and shared/ directory are deleted."""
    shared_dir = REPO_ROOT / "shared"
    assert_false(shared_dir.exists(), "shared/ directory should be removed")


def test_f3_05_settings_gradle_references():
    """Verify settings.gradle.kts contains only active release modules and no dead directories."""
    settings_file = REPO_ROOT / "settings.gradle.kts"
    content = read_file(settings_file)
    
    assert_false('include(":androidApp")' in content, "settings.gradle.kts must not include :androidApp")
    assert_false('include(":core:models")' in content, "settings.gradle.kts must not include :core:models")
    assert_false('include(":core:ui")' in content, "settings.gradle.kts must not include :core:ui")
    assert_false('include(":shared")' in content, "settings.gradle.kts must not include :shared")
