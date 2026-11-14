# ExpenseShare Release Reference & Checklist

## Multiplatform Release Architecture

ExpenseShare consists of several deployment targets:
1. **Server (Ktor)**: Runs on Liara Cloud (`expenseshare-server.liara.run`) with PostgreSQL 16 (`expenseshare-db`).
2. **Telegram Mini App (WasmJs)**: Hosted on GitHub Pages (`https://cloner93.github.io/ExpenseShare/`).
3. **Android Client**: Packaged as release APK signed with debug keystore.
4. **Desktop Clients**: 
   - Linux: Debian package (`.deb`)
   - macOS: Apple disk image (`.dmg`)
   - Windows: Windows installer (`.msi`)
5. **iOS Client**: Kotlin Multiplatform shared framework with SwiftUI wrapper (`Config.xcconfig`).

## CI/CD Workflows

The repository has three GitHub Actions workflows:

1. `.github/workflows/deploy-server.yml`:
   - Trigger: Push to `main` touching server, core, Dockerfile, or workflow files; or `workflow_dispatch`.
   - Actions: Builds server distribution (`./gradlew :server:installDist`), builds and pushes Docker image to GHCR, and deploys to Liara using `@liara/cli` with `LIARA_API_TOKEN`.

2. `.github/workflows/deploy-mini-app.yml`:
   - Trigger: Push to `main` touching web / wasmJs code; or `workflow_dispatch`.
   - Actions: Builds WasmJs production distribution (`./gradlew :composeApp:wasmJsBrowserDistribution`), configures GitHub Pages path base, and deploys to GitHub Pages.

3. `.github/workflows/release-clients.yml`:
   - Trigger: Push of tag matching `v*` (e.g. `v1.0.2`), or `workflow_dispatch` with `tag_name`.
   - Actions:
     - `build-android-and-linux`: Assembles release APK & packages `.deb`
     - `build-macos`: Packages `.dmg`
     - `build-windows`: Packages `.msi`
     - `publish-release`: Downloads all artifacts, creates GitHub Release, attaches APK, DEB, DMG, MSI.

## Version Numbering Rules

- Follow Semantic Versioning: `MAJOR.MINOR.PATCH` (e.g., `1.0.2`).
- Desktop MSI requires strictly 3-component versions (no pre-release suffixes like `-alpha` in `packageVersion`).
- Android `versionCode` must strictly increase monotonically (integer).
