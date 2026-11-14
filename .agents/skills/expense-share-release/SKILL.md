---
name: expense-share-release
description: >-
  Release automation and multiplatform deployment orchestration for ExpenseShare.
  Use this skill when releasing a new version, bumping project version numbers across
  Android, Desktop, iOS, and Server, running pre-release checks, tagging and pushing to GitHub,
  or verifying CI/CD deployment status across Liara server, Telegram Mini App, and GitHub Releases.
---

# ExpenseShare Release Automation Runbook

This skill automates the full release lifecycle of ExpenseShare across all targets:
- Backend Server on Liara Cloud (`expenseshare-server.liara.run`)
- Telegram Mini App on GitHub Pages (`https://cloner93.github.io/ExpenseShare/`)
- Multiplatform Client Binaries on GitHub Releases (Android APK, Linux DEB, macOS DMG, Windows MSI)

---

## Release Procedure

Follow these steps in order when the user asks to release a new version.

### Step 1: Determine and Bump Version Numbers

Run the version bumping script with the desired target semantic version (e.g. `1.0.2`):

```bash
python3 .agents/skills/expense-share-release/scripts/bump_version.py <TARGET_VERSION>
```

This automatically synchronizes:
- **Android**: `versionCode` and `versionName` in `build-logic/convention/src/main/kotlin/org/milad/expense_share/plugins/AndroidApplicationConventionPlugin.kt`
- **Desktop (JVM)**: `packageVersion` in `composeApp/build.gradle.kts`
- **Server**: `version` in `server/build.gradle.kts`
- **iOS**: `CURRENT_PROJECT_VERSION` and `MARKETING_VERSION` in `iosApp/Configuration/Config.xcconfig`

Use `--dry-run` to preview changes without modifying files if the user is only asking to inspect.

### Step 2: Run Pre-Release Tests & Checks

Execute tests to ensure all shared logic and server routes pass:

```bash
./gradlew test :server:test --no-daemon
```

Optionally verify Android release APK compilation locally:

```bash
./gradlew :composeApp:assembleRelease --no-daemon
```

### Step 3: Check Database Migrations & Environment Variables

- Check if any new Exposed tables, columns, or relations were added in `server/src/main/kotlin/org/milad/expense_share/data/db/`.
- Ensure changes are backward compatible with currently installed client versions.
- If new environment variables or secret keys were introduced, confirm they are set in the Liara Console (`https://console.liara.ir`).

### Step 4: Commit & Push to Main

Commit all updated files and push to `origin main`:

```bash
git add -u
git commit -m "chore(release): bump version to <TARGET_VERSION>"
git push origin main
```

> **Note**: Pushing to `main` automatically triggers:
> 1. `.github/workflows/deploy-server.yml` (builds Docker image, updates GHCR, deploys to Liara)
> 2. `.github/workflows/deploy-mini-app.yml` (builds WasmJs bundle, deploys to GitHub Pages)

### Step 5: Tag the Release & Trigger Multiplatform Client Release

Tag the commit with semantic format `v<TARGET_VERSION>` and push to remote:

```bash
git tag v<TARGET_VERSION>
git push origin v<TARGET_VERSION>
```

> **Note**: Pushing a tag starting with `v*` automatically triggers `.github/workflows/release-clients.yml`, which runs 3 parallel matrix jobs (`build-android-and-linux`, `build-macos`, `build-windows`) and then creates the official GitHub Release (`publish-release`).

### Step 6: Monitor Deployment & Workflows

Monitor the running workflows using `gh`:

```bash
gh run list --limit 3
```

Watch the specific client release workflow until completion:

```bash
gh run watch <RUN_ID> --exit-status
```

### Step 7: Post-Release Verification

Run the verification script to validate live endpoints and GitHub Release artifacts:

```bash
python3 .agents/skills/expense-share-release/scripts/verify_release.py --tag v<TARGET_VERSION>
```

Verify that:
- Server responds with HTTP 404/200 on `https://expenseshare-server.liara.run/`
- Telegram Mini App responds with HTTP 200 on `https://cloner93.github.io/ExpenseShare/`
- GitHub Release contains 4 assets:
  - `ExpenseShare-v<TARGET_VERSION>.apk`
  - `org.milad.expense-share_<TARGET_VERSION>_amd64.deb`
  - `org.milad.expense_share-<TARGET_VERSION>.dmg`
  - `org.milad.expense_share-<TARGET_VERSION>.msi`
