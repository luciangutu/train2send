#!/bin/bash
set -euo pipefail

GRADLE_FILE="app/build.gradle.kts"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# --- Extract versions from build.gradle.kts ---
CURRENT_VERSION_CODE=$(grep -oP 'versionCode\s*=\s*\K\d+' "$GRADLE_FILE")
CURRENT_VERSION_NAME=$(grep -oP 'versionName\s*=\s*"\K[^"]+' "$GRADLE_FILE")

if [[ -z "$CURRENT_VERSION_CODE" || -z "$CURRENT_VERSION_NAME" ]]; then
    echo "❌ Could not parse versionCode or versionName from $GRADLE_FILE"
    exit 1
fi

echo "📋 build.gradle.kts versions:"
echo "   versionCode = $CURRENT_VERSION_CODE"
echo "   versionName = $CURRENT_VERSION_NAME"

# --- Get latest git tag ---
LATEST_TAG=$(git tag --sort=-v:refname | head -n 1)

if [[ -z "$LATEST_TAG" ]]; then
    echo "⚠️  No git tags found. Assuming this is the first release."
    echo ""
else
    # Strip leading 'v' from tag if present (e.g., v1.3.6 -> 1.3.6)
    TAG_VERSION="${LATEST_TAG#v}"

    echo ""
    echo "🏷️  Latest git tag: $LATEST_TAG (version: $TAG_VERSION)"

    # --- Compare versionName ---
    if [[ "$CURRENT_VERSION_NAME" == "$TAG_VERSION" ]]; then
        echo ""
        echo "❌ versionName ($CURRENT_VERSION_NAME) has NOT been incremented from the latest tag ($LATEST_TAG)."
        echo "   Please bump versionName in $GRADLE_FILE before building a release."
        exit 1
    fi

    # Compare versions to make sure current is higher
    version_gt() {
        # Returns 0 (true) if $1 > $2 using version sort
        [[ "$(printf '%s\n%s' "$1" "$2" | sort -V | tail -n 1)" == "$1" ]]
    }

    if ! version_gt "$CURRENT_VERSION_NAME" "$TAG_VERSION"; then
        echo ""
        echo "❌ versionName ($CURRENT_VERSION_NAME) is not higher than the latest tag ($TAG_VERSION)."
        echo "   Please set a higher versionName in $GRADLE_FILE."
        exit 1
    fi

    # --- Compare versionCode ---
    # Try to get versionCode from the tagged commit's build.gradle.kts
    TAGGED_VERSION_CODE=$(git show "$LATEST_TAG:$GRADLE_FILE" 2>/dev/null | grep -oP 'versionCode\s*=\s*\K\d+' || echo "")

    if [[ -n "$TAGGED_VERSION_CODE" ]]; then
        if [[ "$CURRENT_VERSION_CODE" -le "$TAGGED_VERSION_CODE" ]]; then
            echo ""
            echo "❌ versionCode ($CURRENT_VERSION_CODE) has NOT been incremented from the tagged version ($TAGGED_VERSION_CODE)."
            echo "   Please bump versionCode in $GRADLE_FILE before building a release."
            exit 1
        fi
    fi

    echo ""
    echo "✅ Versions are incremented: $TAG_VERSION → $CURRENT_VERSION_NAME (code: $CURRENT_VERSION_CODE)"
fi

# --- Check local.properties for keystore passwords ---
if [[ ! -f "local.properties" ]]; then
    echo ""
    echo "❌ local.properties not found. Please create it with keystore.password and key.password."
    exit 1
fi

if ! grep -q "keystore.password" local.properties || ! grep -q "key.password" local.properties; then
    echo ""
    echo "❌ local.properties is missing keystore.password or key.password."
    exit 1
fi

# --- Build the release bundle ---
echo ""
echo "🔨 Building release bundle..."
echo ""

./gradlew :app:bundleRelease

AAB_DIR="app/build/outputs/bundle/release"
mv "$AAB_DIR/app-release.aab" "$AAB_DIR/train2send-${CURRENT_VERSION_NAME}.aab"

echo ""
echo "✅ Release bundle built: $AAB_DIR/train2send-${CURRENT_VERSION_NAME}.aab"
echo ""

# --- Git: commit, tag, push ---
echo "📦 Committing and tagging..."
git add .
git commit -m "Release $CURRENT_VERSION_NAME"
git push
git tag "v$CURRENT_VERSION_NAME"
git push origin "v$CURRENT_VERSION_NAME"

echo ""
echo "✅ Tagged and pushed v$CURRENT_VERSION_NAME"

# --- Create GitHub release and upload AAB ---
echo ""
echo "🚀 Creating GitHub release..."

AAB_FILE="$AAB_DIR/train2send-${CURRENT_VERSION_NAME}.aab"

gh release create "v$CURRENT_VERSION_NAME" \
    "$AAB_FILE" \
    --title "v$CURRENT_VERSION_NAME" \
    --generate-notes

echo ""
echo "✅ Done! GitHub release v$CURRENT_VERSION_NAME created with AAB uploaded."
