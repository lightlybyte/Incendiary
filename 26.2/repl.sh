#!/usr/bin/env bash
set -euo pipefail

# ============================================================
#  Config
# ============================================================
JAVA_EXE="JRE/bin/java.exe"          # use "JRE/bin/java" on Linux/macOS
VINEFLOWER_JAR="essentials/decompiler.jar"
THREAD_COUNT="${THREAD_COUNT:-8}"
JAVA_MEM="${JAVA_MEM:-4G}"

CLIENT_URL="https://piston-data.mojang.com/v1/objects/2dc72797acbc1b63fc16a11c4ac393605f453754/client.jar"
SERVER_URL="https://piston-data.mojang.com/v1/objects/823e2250d24b3ddac457a60c92a6a941943fcd6a/server.jar"

CLIENT_JAR="client_26.2.jar"; SERVER_JAR="server_26.2.jar"
CLIENT_DIR="client-26.2";     SERVER_DIR="server-26.2"

# ============================================================
#  Helpers
# ============================================================
usage() {
    cat <<EOF
Usage: $(basename "$0") <command>

  downloadClientJar | downloadServerJar
  extractClientJar  | extractServerJar
  decompileClientJar | decompileServerJar
  getClient | getServer | getBoth
EOF
    exit 1
}

resolve_kind() {
    if [[ "$1" == "client" ]]; then
        URL="$CLIENT_URL"; JAR="$CLIENT_JAR"; DIR="$CLIENT_DIR"
    else
        URL="$SERVER_URL"; JAR="$SERVER_JAR"; DIR="$SERVER_DIR"
    fi
}

# ============================================================
#  Download
# ============================================================
download() {
    local kind="$1"; resolve_kind "$kind"
    [[ -f "$JAR" ]] && { echo "[=] $JAR already exists, skipping."; return 0; }

    echo "[*] Downloading $kind..."
    if command -v curl >/dev/null 2>&1; then
        curl -L --fail --progress-bar -o "$JAR.part" "$URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$JAR.part" "$URL"
    else
        echo "[!] Need curl or wget."; exit 1
    fi
    mv "$JAR.part" "$JAR"
    echo "[OK] Saved $JAR"
}

# ============================================================
#  Extract
# ============================================================
extract() {
    local kind="$1"; resolve_kind "$kind"
    [[ -f "$JAR" ]] || { echo "[!] $JAR not found."; exit 1; }
    mkdir -p "$DIR"

    echo "[*] Extracting $JAR -> $DIR/"
    if command -v unzip >/dev/null 2>&1; then
        unzip -q -o "$JAR" -d "$DIR"; tool=unzip
    elif command -v 7z >/dev/null 2>&1; then
        7z x -y -o"$DIR" "$JAR" >/dev/null; tool=7z
    elif command -v jar >/dev/null 2>&1; then
        (cd "$DIR" && jar xf "../$JAR"); tool=jar
    elif [[ -x "$JAVA_EXE" ]]; then
        jar_exe="$(dirname "$JAVA_EXE")/jar"
        (cd "$DIR" && "$jar_exe" xf "../$JAR"); tool="jar (bundled)"
    else
        echo "[!] Need unzip, 7z, or jar."; exit 1
    fi

    n=$(find "$DIR" -type f | wc -l | tr -d ' ')
    echo "[OK] Extracted $n files using $tool"
}

# ============================================================
#  Decompile  -- zip output + native extract
# ============================================================
decompile() {
    local kind="$1"; resolve_kind "$kind"
    local out="${DIR}-src"
    local zip="${DIR}-src.zip"

    [[ -d "$DIR" ]]            || { echo "[!] $DIR not found."; exit 1; }
    [[ -x "$JAVA_EXE" ]]       || { echo "[!] Java not found at $JAVA_EXE"; exit 1; }
    [[ -f "$VINEFLOWER_JAR" ]] || { echo "[!] Vineflower not found at $VINEFLOWER_JAR"; exit 1; }

    rm -rf "$out" "$zip"
    mkdir -p "$out"

    total=$(find "$DIR" -name '*.class' | wc -l | tr -d ' ')
    [[ "$total" -gt 0 ]] || { echo "[!] No .class files under $DIR"; exit 1; }
    echo "[*] $total classes -> $zip, $THREAD_COUNT threads"

    # --- Phase 1: Vineflower writes a zip in the background ---
    "$JAVA_EXE" -Xmx"$JAVA_MEM" -jar "$VINEFLOWER_JAR" \
        --folder --thread-count="$THREAD_COUNT" \
        "$DIR" "$zip" \
        >/dev/null 2>&1 &
    pid=$!

    t0=$(date +%s)
    while kill -0 "$pid" 2>/dev/null; do
        if [[ -f "$zip" ]]; then
            size=$(( $(stat -c%s "$zip" 2>/dev/null || stat -f%z "$zip" 2>/dev/null || echo 0) / 1024 ))
            elapsed=$(( $(date +%s) - t0 ))
            printf "\r  [..] writing zip: %d KB (%ds)" "$size" "$elapsed"
        else
            printf "\r  [..] starting JVM... (%ds)" "$(( $(date +%s) - t0 ))"
        fi
        sleep 0.5
    done
    wait "$pid"; rc=$?
    printf "\r%-60s\r" ""

    [[ $rc -ne 0 ]] && { echo "[!] Vineflower exited with code $rc"; exit $rc; }
    [[ -f "$zip" ]] || { echo "[!] Vineflower didn't produce $zip"; exit 1; }

    zip_mb=$(( $(stat -c%s "$zip" 2>/dev/null || stat -f%z "$zip") / 1048576 ))
    echo "[OK] Zip written: $zip (${zip_mb} MB)"

    # --- Phase 2: native extract ---
    echo "[*] Extracting $zip -> $out/ ..."
    if command -v unzip >/dev/null 2>&1; then
        unzip -q -o "$zip" -d "$out"; tool=unzip
    elif command -v 7z >/dev/null 2>&1; then
        7z x -y -o"$out" "$zip" >/dev/null; tool=7z
    elif command -v jar >/dev/null 2>&1; then
        (cd "$out" && jar xf "../$zip"); tool=jar
    elif [[ -x "$JAVA_EXE" ]]; then
        jar_exe="$(dirname "$JAVA_EXE")/jar"
        (cd "$out" && "$jar_exe" xf "../$zip"); tool="jar (bundled)"
    else
        echo "[!] Need unzip, 7z, or jar to extract."; exit 1
    fi

    count=$(find "$out" -name '*.java' | wc -l | tr -d ' ')
    dt=$(( $(date +%s) - t0 ))
    echo "[OK] Extracted with $tool: $count/$total classes in ${dt}s"
    echo "[OK] Source in $out/"
}

# ============================================================
#  Dispatch
# ============================================================
case "${1:-}" in
    downloadClientJar)  download client ;;
    downloadServerJar)  download server ;;
    extractClientJar)   extract  client ;;
    extractServerJar)   extract  server ;;
    decompileClientJar) decompile client ;;
    decompileServerJar) decompile server ;;
    getClient)          download client && extract client && decompile client ;;
    getServer)          download server && extract server && decompile server ;;
    getBoth)
        download client && extract client
        download server && extract server
        decompile client && decompile server
        ;;
    *) usage ;;
esac