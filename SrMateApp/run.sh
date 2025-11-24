#!/bin/bash

# srMATE - College Friend Matcher Launch Script
# Compatible with MariaDB/MySQL

JDBC="/usr/share/java/mysql-connector-j.jar"

echo "🎯 srMATE - Find Your College Connection"
echo "========================================"

# Check if database is running
if ! systemctl is-active --quiet mariadb && ! systemctl is-active --quiet mysql; then
    echo "⚠️  Warning: MariaDB/MySQL may not be running"
    echo "   Start it with: sudo systemctl start mariadb"
fi

echo "📦 Compiling SrMateApp.java..."
javac -cp .:$JDBC SrMateApp.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✅ Compilation successful!"
echo ""
echo "🚀 Launching srMATE application..."
echo "   Database: localhost:3306/srmatedb"
echo ""

# --- Hyprland / Wayland compatibility setup ---
export _JAVA_AWT_WM_NONREPARENTING=1
export GDK_BACKEND=x11

# --- Detect Hyprland fractional scale ---
if command -v hyprctl >/dev/null 2>&1 && command -v jq >/dev/null 2>&1; then
    SCALE=$(hyprctl monitors -j | jq '.[0].scale')
    DPI=$(echo "96 * $SCALE" | bc)
    export Xft.dpi=${DPI%.*}
    echo "📏 Detected Hyprland scale: $SCALE (DPI=$Xft.dpi)"
else
    export Xft.dpi=120   # Default to 1.25× scale
    echo "📏 Default DPI set to $Xft.dpi (1.25×)"
fi

# --- Choose best Java runtime (JetBrains JBR if available) ---
if [ -x "/usr/lib/jbrsdk/bin/java" ]; then
    JAVA_BIN="/usr/lib/jbrsdk/bin/java"
    echo "☕ Using JetBrains Runtime (JBR)"
else
    JAVA_BIN="java"
    echo "☕ Using system Java"
fi

echo ""
echo "💝 Starting srMATE..."
echo "========================================"

# --- Launch app with HiDPI-aware rendering ---
"$JAVA_BIN" \
    -Dsun.java2d.dpiaware=false \
    -Dsun.java2d.uiScale=1.0 \
    -Dswing.aatext=true \
    -Dawt.useSystemAAFontSettings=on \
    -Dsun.java2d.xrender=true \
    -cp .:$JDBC \
    SrMateApp

echo ""
echo "👋 srMATE closed. Thanks for using!"
