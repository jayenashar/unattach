#!/usr/bin/env bash

set -e -u -x -o pipefail

mvn clean package

# https://docs.oracle.com/en/java/javase/14/jpackage/support-application-features.html
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
  rm -rf ./*.deb
  jpackage --name Slimbox --app-version 1.7.0 \
    --description "Easily download email attachments in bulk, and optionally remove them from the original emails." \
    --vendor "Rok Strniša" --copyright "Copyright 2020, All rights reserved" \
    --license-file LICENSE \
    --icon src/main/resources/logo-256.png \
    --linux-shortcut \
    --input target \
    --java-options "-Xmx1024m" \
    --main-jar slimbox-1.7.0-jar-with-dependencies.jar \
    --main-class com.strnisa.rok.slimbox.Main
elif [[ "$OSTYPE" == "darwin"* ]]; then
  rm -rf ./*.dmg
  jpackage --name Slimbox --app-version 1.7.0 \
    --description "Easily download email attachments in bulk, and optionally remove them from the original emails." \
    --vendor "Rok Strniša" --copyright "Copyright 2020, All rights reserved" \
    --license-file LICENSE \
    --icon src/main/resources/logo-256.icns \
    --input target \
    --java-options "-Xmx1024m" \
    --main-jar slimbox-1.7.0-jar-with-dependencies.jar \
    --main-class com.strnisa.rok.slimbox.Main
elif [[ "$OSTYPE" == "msys" ]]; then
  rm -rf ./*.exe
  jpackage --name Slimbox --app-version 1.7.0 \
    --description "Easily download email attachments in bulk, and optionally remove them from the original emails." \
    --vendor "Rok Strniša" --copyright "Copyright 2020, All rights reserved" \
    --license-file LICENSE \
    --icon src/main/resources/logo-256.ico \
    --win-shortcut --win-dir-chooser \
    --input target \
    --java-options "-Xmx1024m" \
    --main-jar slimbox-1.7.0-jar-with-dependencies.jar \
    --main-class com.strnisa.rok.slimbox.Main
else
  echo "Unsupported system: $OSTYPE"
fi
