#!/usr/bin/env bash

set -e -u -x -o pipefail

mvn clean package

VERSION=2.0.0
DESCRIPTION="Easily download Gmail attachments in bulk, and optionally remove them."
VENDOR="Rok Strniša"
COPYRIGHT="Copyright 2020, All rights reserved"
MAIN_CLASS="app.unattach.Main"
JAVA_OPTION="-Xmx2000m"
JAR_PATH=$(ls target/*-with-dependencies.jar)
JAR_FILE=$(basename "$JAR_PATH")

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  rm -rf ./*.deb
  jpackage --name Unattach --app-version $VERSION --description "$DESCRIPTION" --vendor "$VENDOR" --copyright "$COPYRIGHT" \
    --license-file LICENSE \
    --icon src/main/resources/logo-256.png \
    --linux-shortcut \
    --input target \
    --java-options "$JAVA_OPTION" \
    --main-jar "$JAR_FILE" \
    --main-class $MAIN_CLASS
elif [[ "$OSTYPE" == "darwin"* ]]; then
  # Re-pack JAR with signed libraries.
  rm -rf tmp
  mkdir tmp
  mv "$JAR_PATH" tmp/
  pushd tmp
  jar xf "$JAR_FILE"
  rm "$JAR_FILE"
  ls *.dylib | xargs codesign -s "Developer ID Application: Rok Strnisa (73XQUXV944)" -f -v
  jar cmf META-INF/MANIFEST.MF "../$JAR_PATH" ./*
  popd
  # Create APP.
  rm -rf Unattach.app
  jpackage --name Unattach --app-version $VERSION --description "$DESCRIPTION" --vendor "$VENDOR" --copyright "$COPYRIGHT" \
    --icon src/main/resources/logo-256.icns \
    --input target \
    --main-jar "$JAR_FILE" \
    --main-class $MAIN_CLASS \
    --java-options "$JAVA_OPTION" \
    --type app-image
  # Sign APP's runtime and itself.
  codesign -s "Developer ID Application: Rok Strnisa (73XQUXV944)" --options runtime --entitlements macos.entitlements -f -v Unattach.app/Contents/runtime/Contents/MacOS/libjli.dylib
  codesign -s "Developer ID Application: Rok Strnisa (73XQUXV944)" --options runtime --entitlements macos.entitlements -f -v Unattach.app/Contents/MacOS/Unattach
  codesign -s "Developer ID Application: Rok Strnisa (73XQUXV944)" --options runtime --entitlements macos.entitlements -f -v Unattach.app
  # Create DMG.
  rm -rf ./*.dmg
  jpackage --name Unattach --app-version $VERSION --description "$DESCRIPTION" --vendor "$VENDOR" --copyright "$COPYRIGHT" \
    --license-file LICENSE \
    --type dmg --app-image Unattach.app
  # Sign DMG.
  codesign -s "Developer ID Application: Rok Strnisa (73XQUXV944)" --options runtime --entitlements macos.entitlements -vvvv --deep Unattach-$VERSION.dmg
  # Upload DMG for verification.
  REQUEST_UUID=$(xcrun altool --notarize-app --primary-bundle-id "app.unattach-$VERSION" -u "rok.strnisa@gmail.com" -p "@keychain:Unattach_APP_PASSWORD" --file Unattach-$VERSION.dmg | grep RequestUUID | awk '{print $3}')
  # Wait for verification to complete.
  while xcrun altool --notarization-info "$REQUEST_UUID" -u rok.strnisa@gmail.com -p "@keychain:Unattach_APP_PASSWORD" | grep "Status: in progress" > /dev/null; do
    echo "Verification in progress..."
    sleep 30
  done
  # Attach stamp to the DMG.
  xcrun stapler staple Unattach-$VERSION.dmg
  # Check APP and DMG.
  spctl -vvv --assess --type exec Unattach.app
  codesign -vvv --deep --strict Unattach-$VERSION.dmg
  codesign -dvv Unattach-$VERSION.dmg
elif [[ "$OSTYPE" == "msys" ]]; then
  rm -rf ./*.exe
  jpackage --name Unattach --app-version $VERSION --description "$DESCRIPTION" --vendor "$VENDOR" --copyright "$COPYRIGHT" \
    --license-file LICENSE \
    --icon src/main/resources/logo-256.ico \
    --win-shortcut --win-dir-chooser \
    --input target \
    --java-options "$JAVA_OPTION" \
    --main-jar "$JAR_FILE" \
    --main-class app.unattach.Main
else
  echo "Unsupported system: $OSTYPE"
fi
