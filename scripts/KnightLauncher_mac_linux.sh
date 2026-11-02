#!/bin/sh

# Starts Knight Launcher, looking for a Java to start it with in this order:
# 1. The launcher's own Java runtime, in KnightLauncher/runtime.
# 2. Any Java installed on this computer. The launcher downloads its own runtime if needed.
# 3. The Java runtime the game ships with.

INSTDIR=$(cd "$(dirname "$0")" && pwd)
cd "$INSTDIR" || exit 1

JAVA=""

if [ -f "KnightLauncher/runtime.properties" ]; then
  RUNTIME_DIR=$(sed -n 's/^dir=//p' "KnightLauncher/runtime.properties" | tr -d '\r')
  if [ -n "$RUNTIME_DIR" ] && [ -x "KnightLauncher/runtime/$RUNTIME_DIR/bin/java" ]; then
    JAVA="$INSTDIR/KnightLauncher/runtime/$RUNTIME_DIR/bin/java"
  fi
fi

if [ -z "$JAVA" ] && command -v java >/dev/null 2>&1; then
  JAVA="java"
fi

if [ -z "$JAVA" ]; then
  for CANDIDATE in "../runtime" "java_vm" "java"; do
    if [ -x "$CANDIDATE/bin/java" ]; then
      JAVA="$INSTDIR/$CANDIDATE/bin/java"
      break
    fi
  done
fi

if [ -z "$JAVA" ]; then
  echo "Knight Launcher needs Java to start, but none was found." >&2
  exit 1
fi

exec "$JAVA" -jar "$INSTDIR/KnightLauncher.jar"
