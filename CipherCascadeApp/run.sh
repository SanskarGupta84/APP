#!/bin/bash
JDBC="/usr/share/java/mysql-connector-j.jar"

echo "Compiling..."
javac -cp .:$JDBC CipherCascadeApp.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✅ Compilation successful. Launching app..."

# --- Hyprland / Wayland compatibility setup ---
export _JAVA_AWT_WM_NONREPARENTING=1
export GDK_BACKEND=x11
export QT_QPA_PLATFORM=xcb
export XDG_SESSION_TYPE=x11

# --- Detect Hyprland fractional scale ---
if command -v hyprctl >/dev/null 2>&1 && command -v jq >/dev/null 2>&1; then
    SCALE=$(hyprctl monitors -j | jq -r '.[0].scale')
    DPI=$(awk "BEGIN {print int(96 * $SCALE)}")
    echo "📏 Detected Hyprland scale: $SCALE (DPI=$DPI)"
else
    SCALE=1.25
    DPI=120
    echo "📏 Default DPI set to $DPI (1.25×)"
fi

# --- Choose best Java runtime (JetBrains JBR if available) ---
if [ -x "/usr/lib/jbrsdk/bin/java" ]; then
    JAVA_BIN="/usr/lib/jbrsdk/bin/java"
    echo "☕ Using JetBrains Runtime (JBR)"
else
    JAVA_BIN="java"
    echo "☕ Using system Java"
fi

# --- Launch app with HiDPI-aware rendering ---
"$JAVA_BIN" \
    -Dsun.java2d.uiScale.enabled=false \
    -Dsun.java2d.dpiaware=true \
    -Dswing.aatext=true \
    -Dawt.useSystemAAFontSettings=on \
    -Dsun.java2d.opengl=true \
    -Dsun.java2d.xrender=true \
    -Dawt.font.desktophints=on \
    -Dprism.lcdtext=true \
    -Dprism.text=t2k \
    -cp .:$JDBC \
    CipherCascadeApp
