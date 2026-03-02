#!/bin/bash

# 1. Configuration
APP_NAME="Tessera"
MAIN_CLASS="com.tessera.engine.client.ClientWindow"
INPUT_DIR="out/artifacts/Tessera_jar"
MAIN_JAR="Tessera.jar"
DEST_DIR="dist"

# 2. Detect OS and set Type
# We use 'app-image' for Linux/Mac to get a portable executable folder
# We keep 'exe' for Windows to get a single installer
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
    TYPE="exe"
    PLATFORM_FLAGS="--win-shortcut --win-menu"
    echo "--- Building Windows Installer (.exe) ---"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    TYPE="app-image"
    PLATFORM_FLAGS=""
    echo "--- Building macOS Portable App ---"
else
    TYPE="app-image"
    PLATFORM_FLAGS="" # app-image doesn't support shortcuts via jpackage
    echo "--- Building Linux Portable Executable Folder ---"
fi

# 3. Execution
# Note: $JAVA_HOME must point to a JDK, not just a JRE
jpackage \
  --type "$TYPE" \
  --dest "$DEST_DIR" \
  --name "$APP_NAME" \
  --input "$INPUT_DIR" \
  --main-jar "$MAIN_JAR" \
  --main-class "$MAIN_CLASS" \
  $PLATFORM_FLAGS \
  --verbose \
  --runtime-image "$JAVA_HOME"

echo "--- Build Complete ---"
if [[ "$TYPE" == "app-image" ]]; then
    echo "Your executable is located at: $DEST_DIR/$APP_NAME/bin/$APP_NAME"
fi