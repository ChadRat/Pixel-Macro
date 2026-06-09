#!/bin/bash
echo "Building debug APK..."
gradle :app:assembleDebug
if [ $? -eq 0 ]; then
    echo "Copying APK to project root..."
    cp app/build/outputs/apk/debug/app-debug.apk ./app-debug.apk
    echo "Done! The debug APK is available at ./app-debug.apk"
else
    echo "Build failed!"
    exit 1
fi
