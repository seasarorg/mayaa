#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TEST_WAR_DIR="$ROOT_DIR/test-war"

MAYAA_VERSION="${MAYAA_VERSION:-2.0.0-SNAPSHOT}"
APP_PORT="${APP_PORT:-8080}"
JMX_HOSTNAME="${JMX_HOSTNAME:-127.0.0.1}"
JFR_SETTINGS="${JFR_SETTINGS:-profile}"
APP_MODE="${APP_MODE:-spring-boot-run}"
SPRING_BOOT_RUN_JVM_ARGS="${SPRING_BOOT_RUN_JVM_ARGS:--Djava.awt.headless=true}"
READY_TIMEOUT_SEC="${READY_TIMEOUT_SEC:-120}"
READY_PATHS="${READY_PATHS:-/index.html,/tests/index.html}"
READY_MODE="${READY_MODE:-http}"
JMETER_THREAD="${JMETER_THREAD:-20}"
JMETER_RAMPUP="${JMETER_RAMPUP:-20}"
JMETER_DURATION="${JMETER_DURATION:-180}"
JMETER_DELAY="${JMETER_DELAY:-15}"
PERF_MIX_BALANCED="${PERF_MIX_BALANCED:-5}"
PERF_MIX_CACHE_MISS="${PERF_MIX_CACHE_MISS:-3}"
PERF_MIX_MEMORY="${PERF_MIX_MEMORY:-2}"
PERF_MIX_SYNC_HOT="${PERF_MIX_SYNC_HOT:-0}"
PERF_MIX_SYNC_MISS="${PERF_MIX_SYNC_MISS:-0}"
PERF_MIX_MAYAA_COLD="${PERF_MIX_MAYAA_COLD:-0}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
JFR_FILE="${JFR_FILE:-$ROOT_DIR/flight_recording_${TIMESTAMP}.jfr}"
APP_LOG="${APP_LOG:-$ROOT_DIR/tmp/spring-boot-jfr-${TIMESTAMP}.log}"
APP_JAVA_PID=""

mkdir -p "$ROOT_DIR/tmp"

if [[ "$APP_PORT" == "auto" ]]; then
  SELECTED_PORT=""
  for candidate in $(seq 18080 18140); do
    if ! lsof -ti tcp:"$candidate" -sTCP:LISTEN >/dev/null 2>&1; then
      SELECTED_PORT="$candidate"
      break
    fi
  done
  if [[ -z "$SELECTED_PORT" ]]; then
    echo "preflight failed: no free port found in range 18080-18140 for APP_PORT=auto" >&2
    exit 1
  fi
  APP_PORT="$SELECTED_PORT"
  echo "preflight: APP_PORT auto-selected $APP_PORT"
else
  EXISTING_PORT_PID="$(lsof -ti tcp:"$APP_PORT" -sTCP:LISTEN 2>/dev/null | head -n 1 || true)"
  if [[ -n "$EXISTING_PORT_PID" ]]; then
    echo "preflight failed: APP_PORT=$APP_PORT is already in use by pid $EXISTING_PORT_PID" >&2
    echo "stop that process or set APP_PORT to a free port, then retry." >&2
    exit 1
  fi
fi

cleanup() {
  if [[ -n "${APP_PID:-}" ]] && kill -0 "$APP_PID" 2>/dev/null; then
    kill "$APP_PID" >/dev/null 2>&1 || true
    wait "$APP_PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

echo "[1/4] prepare app artifacts (mode=$APP_MODE)"
mvn -f "$TEST_WAR_DIR/pom.xml" clean package -Pspring-boot -DskipTests -Dmayaa.version="$MAYAA_VERSION"

echo "[2/4] start app with JFR -> $JFR_FILE"
if [[ "$APP_MODE" == "jar" ]]; then
  (
    cd "$TEST_WAR_DIR"
    java \
      -XX:StartFlightRecording=filename="$JFR_FILE",settings="$JFR_SETTINGS",dumponexit=true \
      -Dcom.sun.management.jmxremote \
      -Dcom.sun.management.jmxremote.port=9012 \
      -Dcom.sun.management.jmxremote.rmi.port=9012 \
      -Dcom.sun.management.jmxremote.local.only=false \
      -Dcom.sun.management.jmxremote.ssl=false \
      -Dcom.sun.management.jmxremote.authenticate=false \
      -Djava.rmi.server.hostname="$JMX_HOSTNAME" \
      -jar "target/mayaa-package-test-${MAYAA_VERSION}.jar" \
      --server.port="$APP_PORT" \
      >"$APP_LOG" 2>&1
  ) &
  APP_PID=$!
else
  (
    cd "$ROOT_DIR"
    SPRING_BOOT_EFFECTIVE_JVM_ARGS="$SPRING_BOOT_RUN_JVM_ARGS"
    mvn -f "$TEST_WAR_DIR/pom.xml" spring-boot:run -Pspring-boot -DskipTests \
      -Dmayaa.version="$MAYAA_VERSION" \
      -Djmx.hostname="$JMX_HOSTNAME" \
      -Dspring-boot.run.arguments="--server.port=$APP_PORT" \
      -Dspring-boot.jvmArguments="$SPRING_BOOT_EFFECTIVE_JVM_ARGS" \
      >"$APP_LOG" 2>&1
  ) &
  APP_PID=$!
fi

echo "app pid=$APP_PID, log=$APP_LOG"
echo "waiting for app ready..."
IFS=',' read -r -a READY_PATH_ARRAY <<< "$READY_PATHS"
READY_URL=""

is_tcp_ready() {
  (exec 3<>"/dev/tcp/127.0.0.1/${APP_PORT}") >/dev/null 2>&1
}

is_http_ready() {
  for path in "${READY_PATH_ARRAY[@]}"; do
    local code
    code=$(curl -sS --max-time 1 -o /dev/null -w "%{http_code}" "http://127.0.0.1:${APP_PORT}${path}" || true)
    if [[ "$code" =~ ^[23][0-9][0-9]$ ]]; then
      READY_URL="http://127.0.0.1:${APP_PORT}${path} (status=$code)"
      return 0
    fi
  done
  return 1
}

for _ in $(seq 1 "$READY_TIMEOUT_SEC"); do
  if ! kill -0 "$APP_PID" 2>/dev/null; then
    echo "app process exited before ready. see $APP_LOG" >&2
    exit 1
  fi

  if [[ "$READY_MODE" == "tcp" ]]; then
    if is_tcp_ready; then
      READY_URL="tcp://127.0.0.1:${APP_PORT}"
      break
    fi
  else
    if is_http_ready; then
      break
    fi
  fi

  sleep 1
done

if [[ -z "$READY_URL" ]]; then
  echo "app did not become ready in ${READY_TIMEOUT_SEC}s. mode=$READY_MODE paths=$READY_PATHS" >&2
  echo "see $APP_LOG" >&2
  exit 1
fi

echo "app ready at $READY_URL"

if [[ "$APP_MODE" == "spring-boot-run" ]]; then
  for _ in $(seq 1 20); do
    APP_JAVA_PID="$(lsof -ti tcp:"$APP_PORT" -sTCP:LISTEN 2>/dev/null | head -n 1 || true)"
    if [[ -n "$APP_JAVA_PID" ]]; then
      break
    fi
    sleep 1
  done

  for _ in $(seq 1 20); do
    if [[ -n "$APP_JAVA_PID" ]]; then
      break
    fi
    APP_JAVA_PID="$(grep -Eo 'with PID [0-9]+' "$APP_LOG" | awk '{print $3}' | tail -n 1 || true)"
    if [[ -n "$APP_JAVA_PID" ]]; then
      break
    fi
    sleep 1
  done
  if [[ -z "$APP_JAVA_PID" ]]; then
    echo "failed to detect Spring Boot app PID from $APP_LOG" >&2
    exit 1
  fi
  jcmd "$APP_JAVA_PID" JFR.start name=mayaa-profile settings="$JFR_SETTINGS" filename="$JFR_FILE" dumponexit=true >/dev/null
fi

echo "[3/4] run jmeter"
JMETER_EXIT_CODE=0
set +e
(
  cd "$TEST_WAR_DIR"
  mvn jmeter:configure@configuration jmeter:jmeter@jmeter-tests jmeter:results@jmeter-check-results \
    -Dmayaa.version="$MAYAA_VERSION" \
    -Dport="$APP_PORT" \
    -Djmeter.thread="$JMETER_THREAD" \
    -Djmeter.rampup="$JMETER_RAMPUP" \
    -Djmeter.duration="$JMETER_DURATION" \
    -Djmeter.delay="$JMETER_DELAY" \
    -DperfMixBalanced="$PERF_MIX_BALANCED" \
    -DperfMixCacheMiss="$PERF_MIX_CACHE_MISS" \
    -DperfMixMemory="$PERF_MIX_MEMORY" \
    -DperfMixSyncHot="$PERF_MIX_SYNC_HOT" \
    -DperfMixSyncMiss="$PERF_MIX_SYNC_MISS" \
    -DperfMixMayaaCold="$PERF_MIX_MAYAA_COLD"
)
JMETER_EXIT_CODE=$?
set -e

echo "[4/4] stop app and finalize JFR"
if [[ -n "$APP_JAVA_PID" ]] && kill -0 "$APP_JAVA_PID" 2>/dev/null; then
  jcmd "$APP_JAVA_PID" JFR.dump name=mayaa-profile filename="$JFR_FILE" >/dev/null 2>&1 || true
  jcmd "$APP_JAVA_PID" JFR.stop name=mayaa-profile >/dev/null 2>&1 || true
fi
kill "$APP_PID" >/dev/null 2>&1 || true
wait "$APP_PID" >/dev/null 2>&1 || true
APP_PID=""

if [[ -s "$JFR_FILE" ]]; then
  echo "JFR saved: $JFR_FILE"
else
  echo "JFR file was not generated or empty: $JFR_FILE" >&2
  exit 1
fi

if [[ "$JMETER_EXIT_CODE" -ne 0 ]]; then
  echo "JMeter failed with exit code $JMETER_EXIT_CODE" >&2
  exit "$JMETER_EXIT_CODE"
fi
