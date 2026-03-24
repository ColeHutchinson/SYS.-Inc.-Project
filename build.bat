@echo off

echo.
echo MusicCatalog Build Script
echo.

echo.
echo Cleaning project...
ant clean

echo.
echo Compiling project...
ant compile

echo.
echo Running application...
ant run

echo.
echo Done
pause