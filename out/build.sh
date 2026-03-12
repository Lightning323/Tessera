#!/bin/bash

# ==========================================
# DIRECTIONS
# ==========================================
# Place Tessera.jar in the same directory as this file
# BUILDING JAR
# mvn clean package
# To ensure windows builds properly, run  jar tf Tessera.jar | grep "\.dll$"
# If there are real .dll files, the build succeeded.

# ==========================================
# PATH & CONFIGURATION VARIABLES
# ==========================================
# Core Tools
PACKR_JAR="/mnt/SharedVolume/Programs/packr-all-4.0.0.jar"
RCEDIT_EXE="/mnt/SharedVolume/Programs/rcedit-x64.exe"

# Project Folders
RES_SOURCE="../res" # Source folder for resources
EXE_NAME="Tessera.exe" # Name of your Windows executable

# Platform Settings
PLATFORMS=("windows" "mac_x64" "mac_arm64" "linux_x64" "linux_arm64")

# ==========================================
# FUNCTIONS
# ==========================================
pause_exit() {
    echo ""
    read -n 1 -s -r -p "Press any key to exit..."
    exit
    echo ""
}

# Find any java processes running 'Tessera' and kill them so they release file locks
pgrep -f "Tessera" | xargs kill -9 2>/dev/null

#FORCE DELETE THE OUT DIRECTORY
sudo rm -rf ./out

# ==========================================
# MAIN BUILD LOOP
# ==========================================
for PLATFORM in "${PLATFORMS[@]}"; do
    CONFIG="packr_${PLATFORM}.json"
    DEST="out/Tessera ${PLATFORM}"

    echo "------------------------------------"
    echo "Processing Platform: $PLATFORM"
    echo "Using Config: $CONFIG"
    echo "Destination directory: $DEST"
    echo "------------------------------------"


    # Run the Packr command
    if [ -f "$PACKR_JAR" ]; then
        java -jar "$PACKR_JAR" "$CONFIG"
    else
        echo "Error: Packr JAR not found at $PACKR_JAR"
        continue
    fi

    # Check if the Packr command succeeded
    if [ $? -eq 0 ]; then
        # Copy resources if source exists
        if [ -d "$RES_SOURCE" ]; then
            cp -r "$RES_SOURCE" "$DEST"
        fi

        # Platform-specific post-processing
        case $PLATFORM in
            "windows")
                WIN_EXE="$DEST/$EXE_NAME"
                WIN_ICON="$DEST/res/icon.ico"
                
                if [ -f "$WIN_EXE" ] && [ -f "$WIN_ICON" ]; then
                    echo "Applying Windows icon using rcedit..."
                    wine "$RCEDIT_EXE" "$WIN_EXE" --set-icon "$WIN_ICON"
                else
                    echo "Warning: Windows EXE or Icon missing. Skipping rcedit."
                fi
                ;;
            "mac_x64")
                ;;
            "mac_arm")
                ;;
            "linux_x64")
                ;;
            "linux_arm")
                ;;
            *)
                echo "Unknown platform: $PLATFORM"
                ;;
        esac

        echo "Success! Build for $PLATFORM complete."
    else
        echo "Error: Packr failed to build the $PLATFORM executable."
    fi
    echo ""
done

pause_exit