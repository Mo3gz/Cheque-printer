@echo off
echo Building ChequePrinter Installer...

REM Set the path to your JDK bin directory if jpackage is not in your PATH
REM For example: set PATH="C:\Program Files\Java\jdk-21\bin";%PATH%

REM Clean and package the Maven project to create the fat JARs
call mvn clean package

IF %ERRORLEVEL% NEQ 0 (
    echo Maven build failed. Exiting.
    pause
    exit /b %ERRORLEVEL%
)

REM Create the dist directory if it doesn't exist
IF NOT EXIST dist mkdir dist

REM Run jpackage to create the .exe installer
jpackage ^
    --input target ^
    --dest dist ^
    --name ChequePrinter ^
    --main-jar pdfGenerator-1.0-SNAPSHOT.jar ^
    --main-class org.chequePrinter.Main ^
    --type exe ^
    --icon src/main/resources/icon.ico ^
    --app-version 1.0.0 ^
    --vendor "Your Company Name" ^
    --copyright "Copyright (c) 2025 Your Company Name" ^
    --win-shortcut ^
    --win-menu ^
    --win-dir-chooser ^
    --win-upgrade-uuid "597cf72d-7c76-477b-a27e-bbf6e5c9c798" ^
    --win-help-url "https://yourcompany.com/help" ^
    --win-update-url "https://yourcompany.com/updates" ^
    --verbose

IF %ERRORLEVEL% NEQ 0 (
    echo jpackage failed. Exiting.
    pause
    exit /b %ERRORLEVEL%
)

echo Installer built successfully in the 'dist' directory.
pause