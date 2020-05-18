#!/usr/bin/env bash

set -e -u -x -o pipefail

mvn clean package

VERSION=1.8.0
DESCRIPTION="Easily download email attachments in bulk, and optionally remove them from the original emails."
VENDOR="Rok Strniša"
COPYRIGHT="Copyright 2020, All rights reserved"
MAIN_CLASS="com.strnisa.rok.slimbox.Main"
JAR_PATH=`ls target/*-with-dependencies.jar`
JAR_FILE=`basename "$JAR_PATH"`

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  rm -rf ./*.deb
  jpackage --name Slimbox --app-version $VERSION --description "$DESCRIPTION" --vendor "$VENDOR" --copyright "$COPYRIGHT" \
    --license-file LICENSE \
    --icon src/main/resources/logo-256.png \
    --linux-shortcut \
    --input target \
    --java-options "-Xmx1024m" \
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
  jar cmf META-INF/MANIFEST.MF "../$JAR_PATH" *
  popd
  # Create APP.
  rm -rf Slimbox.app
  jpackage --name Slimbox --app-version $VERSION --description "$DESCRIPTION" --vendor "$VENDOR" --copyright "$COPYRIGHT" \
    --icon src/main/resources/logo-256.icns \
    --input target \
    --main-jar "$JAR_FILE" \
    --main-class $MAIN_CLASS \
    --java-options "-Xmx1024m" \
    --type app-image
  # Sign APP's runtime and itself.
  codesign -s "Developer ID Application: Rok Strnisa (73XQUXV944)" --options runtime --entitlements macos.entitlements -f -v Slimbox.app/Contents/runtime/Contents/MacOS/libjli.dylib
  codesign -s "Developer ID Application: Rok Strnisa (73XQUXV944)" --options runtime --entitlements macos.entitlements -f -v Slimbox.app/Contents/MacOS/Slimbox
  codesign -s "Developer ID Application: Rok Strnisa (73XQUXV944)" --options runtime --entitlements macos.entitlements -f -v Slimbox.app
  # Create DMG.
  rm -rf *.dmg
  jpackage --name Slimbox --app-version $VERSION --description "$DESCRIPTION" --vendor "$VENDOR" --copyright "$COPYRIGHT" \
    --license-file LICENSE \
    --type dmg --app-image Slimbox.app
  # Sign DMG.
  codesign -s "Developer ID Application: Rok Strnisa (73XQUXV944)" --options runtime --entitlements macos.entitlements -vvvv --deep Slimbox-$VERSION.dmg
  # Upload DMG for verification.
  REQUEST_UUID=`xcrun altool --notarize-app --primary-bundle-id "com.strnisa.rok.slimbox-$VERSION" -u "rok.strnisa@gmail.com" -p "@keychain:SLIMBOX_APP_PASSWORD" --file Slimbox-$VERSION.dmg | grep RequestUUID | awk '{print $3}'`
  # Wait for verification to complete.
  while xcrun altool --notarization-info $REQUEST_UUID -u rok.strnisa@gmail.com -p "@keychain:SLIMBOX_APP_PASSWORD" | grep "Status: in progress" > /dev/null; do
    echo "Verification in progress..."
    sleep 30
  done
  # Attach stamp to the DMG.
  xcrun stapler staple Slimbox-$VERSION.dmg
  # Check APP and DMG.
  spctl -vvv --assess --type exec Slimbox.app
  codesign -vvv --deep --strict Slimbox-$VERSION.dmg
  codesign -dvv Slimbox-$VERSION.dmg
elif [[ "$OSTYPE" == "msys" ]]; then
  rm -rf ./*.exe
  jpackage --name Slimbox --app-version $VERSION --description "$DESCRIPTION" --vendor "$VENDOR" --copyright "$COPYRIGHT" \
    --license-file LICENSE \
    --icon src/main/resources/logo-256.ico \
    --win-shortcut --win-dir-chooser \
    --input target \
    --java-options "-Xmx1024m" \
    --main-jar "$JAR_FILE" \
    --main-class com.strnisa.rok.slimbox.Main
else
  echo "Unsupported system: $OSTYPE"
fi
