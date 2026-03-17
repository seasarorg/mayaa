#!/usr/bin/env bash
# jfr-analyze.sh  ― JFR 単体分析 & 2ファイル差分比較
#
# 使い方:
#   ./test-war/jfr-analyze.sh  <jfr-file>              # 単体分析
#   ./test-war/jfr-analyze.sh  <jfr-A>  <jfr-B>        # 差分比較 (A=Cold, B=Base など)
#
# 前提: jfr (JDK 付属), jq が PATH に存在すること
set -euo pipefail

# ─── ユーティリティ ───────────────────────────────────────────────────────────

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "ERROR: '$1' が見つかりません。JDK の bin と jq を PATH に追加してください。" >&2; exit 1; }
}
require_cmd jfr
require_cmd jq

# 一時ディレクトリ (終了時に自動削除)
TMPDIR_WORK=$(mktemp -d)
trap 'rm -rf "$TMPDIR_WORK"' EXIT

# 1つの JFR ファイルから指標を JSON として stdout に出力する
# 引数: <jfr-file>  <label>
analyze_jfr() {
  local jfr="$1"
  local label="$2"

  [[ -f "$jfr" ]] || { echo "ERROR: ファイルが見つかりません: $jfr" >&2; exit 1; }

  local slug
  slug=$(basename "$jfr" .jfr | tr -c 'A-Za-z0-9_-' '_')

  # ── JavaMonitorEnter ────────────────────────────────────────────────────────
  local mon_file="$TMPDIR_WORK/${slug}_mon.json"
  jfr print --json --events jdk.JavaMonitorEnter "$jfr" >"$mon_file" 2>/dev/null \
    || echo '{"recording":{"events":[]}}' >"$mon_file"

  # ── GarbageCollection ───────────────────────────────────────────────────────
  local gc_file="$TMPDIR_WORK/${slug}_gc.json"
  jfr print --json --events jdk.GarbageCollection "$jfr" >"$gc_file" 2>/dev/null \
    || echo '{"recording":{"events":[]}}' >"$gc_file"

  # ── ExecutionSample ─────────────────────────────────────────────────────────
  local exec_file="$TMPDIR_WORK/${slug}_exec.json"
  jfr print --json --events jdk.ExecutionSample "$jfr" >"$exec_file" 2>/dev/null \
    || echo '{"recording":{"events":[]}}' >"$exec_file"

  # ── jq で集計 (ファイル経由で渡す) ─────────────────────────────────────────
  jq -n \
    --arg label  "$label" \
    --slurpfile mon  "$mon_file" \
    --slurpfile gc   "$gc_file" \
    --slurpfile exec "$exec_file" \
    '
    # ─ ヘルパ ─────────────────────────────────────
    def ms:   (.values.duration | capture("PT(?<s>[0-9.]+)S").s | tonumber * 1000);
    def mon_cls: (.values.monitorClass.name // "unknown");
    def mayaa_top:
      [.values.stackTrace.frames[]?.method.type.name | select(startswith("org/seasar/mayaa"))][0] // null;
    def has_mayaa:
      ([.values.stackTrace.frames[]?.method.type.name | select(startswith("org/seasar/mayaa"))] | length) > 0;

    # ─ JavaMonitorEnter ───────────────────────────
    ($mon[0].recording.events) as $me |
    ($me | length) as $mc |
    ($me | if $mc == 0 then [] else map(ms) end) as $mms |
    ($mms | if length == 0 then 0 else add end) as $mtot |
    ($mms | sort) as $msorted |
    ($msorted | if length == 0 then 0 else .[(($mc * 0.50) | floor)] end) as $p50 |
    ($msorted | if length == 0 then 0 else .[(($mc * 0.90) | floor)] end) as $p90 |
    ($msorted | if length == 0 then 0 else .[(($mc * 0.99) | floor)] end) as $p99 |
    ($msorted | if length == 0 then 0 else last end) as $pmax |

    # monitorClass 別集計 (上位5)
    ($me | group_by(mon_cls) |
      map({cls: (.[0] | mon_cls), cnt: length, total_ms: (map(ms) | add), avg_ms: ((map(ms) | add) / length)}) |
      sort_by(-.total_ms) | .[0:5]) as $mon_by_cls |

    # Mayaa スタック別集計 (上位5)
    ($me | map(select(mayaa_top != null) | {k: ((mon_cls) + "|" + mayaa_top), d: ms}) |
      group_by(.k) |
      map({key: .[0].k, cnt: length, total_ms: (map(.d) | add), avg_ms: ((map(.d) | add) / length)}) |
      sort_by(-.total_ms) | .[0:5]) as $mon_by_mayaa |

    # ─ GarbageCollection ─────────────────────────
    ($gc[0].recording.events) as $ge |
    ($ge | length) as $gcnt |
    ($ge | if $gcnt == 0 then [] else map(ms) end) as $gcms |
    ($gcms | if length == 0 then 0 else add end) as $gctot |
    ($gcms | if length == 0 then 0 else add / $gcnt end) as $gcavg |
    ($gcms | if length == 0 then 0 else max end) as $gcmax |

    # ─ ExecutionSample ───────────────────────────
    ($exec[0].recording.events) as $ee |
    ($ee | length) as $ecnt |
    ($ee | map(select(has_mayaa)) | length) as $emayaa |
    ($ee | map(select(has_mayaa) | mayaa_top) | group_by(.) |
      map({cls: .[0], cnt: length}) | sort_by(-.cnt) | .[0:8]) as $mayaa_top_classes |

    # ─ 出力オブジェクト ──────────────────────────
    {
      label:   $label,
      monitor: {
        count:     $mc,
        total_ms:  ($mtot | (. * 100 | round) / 100),
        avg_ms:    (if $mc == 0 then 0 else (($mtot / $mc) * 100 | round) / 100 end),
        p50_ms:    ($p50 | (. * 100 | round) / 100),
        p90_ms:    ($p90 | (. * 100 | round) / 100),
        p99_ms:    ($p99 | (. * 100 | round) / 100),
        max_ms:    ($pmax | (. * 100 | round) / 100),
        by_class:  $mon_by_cls,
        by_mayaa:  $mon_by_mayaa
      },
      gc: {
        count:     $gcnt,
        total_ms:  ($gctot | (. * 100 | round) / 100),
        avg_ms:    ($gcavg | (. * 100 | round) / 100),
        max_ms:    ($gcmax | (. * 100 | round) / 100)
      },
      exec_sample: {
        count:        $ecnt,
        mayaa_count:  $emayaa,
        mayaa_ratio:  (if $ecnt == 0 then 0 else (($emayaa * 1000 / $ecnt | round) / 10) end),
        mayaa_top_classes: $mayaa_top_classes
      }
    }
    '
}

# テキスト表示 (1ファイル分の集計 JSON を受け取って整形)
print_report() {
  local json="$1"
  echo "$json" | jq -r '
    "═══════════════════════════════════════════════════════════════",
    "  ファイル : \(.label)",
    "═══════════════════════════════════════════════════════════════",
    "",
    "■ JavaMonitorEnter",
    "  件数        : \(.monitor.count)",
    "  合計待機    : \(.monitor.total_ms) ms",
    "  平均待機    : \(.monitor.avg_ms) ms",
    "  p50         : \(.monitor.p50_ms) ms",
    "  p90         : \(.monitor.p90_ms) ms",
    "  p99         : \(.monitor.p99_ms) ms",
    "  max         : \(.monitor.max_ms) ms",
    "",
    "  ─ monitorClass 内訳 (上位5) ─",
    "  \(["cls","cnt","total_ms","avg_ms"] | @tsv)",
    (.monitor.by_class[] | "  \([.cls, .cnt, (.total_ms | (.*100|round)/100), (.avg_ms | (.*100|round)/100)] | @tsv)"),
    "",
    "  ─ Mayaa スタック内訳 (上位5) ─",
    "  \(["monitorClass|mayaaClass","cnt","total_ms","avg_ms"] | @tsv)",
    (.monitor.by_mayaa[] | "  \([.key, .cnt, (.total_ms | (.*100|round)/100), (.avg_ms | (.*100|round)/100)] | @tsv)"),
    "",
    "■ GarbageCollection",
    "  件数 : \(.gc.count)   合計: \(.gc.total_ms) ms   平均: \(.gc.avg_ms) ms   max: \(.gc.max_ms) ms",
    "",
    "■ ExecutionSample",
    "  総件数     : \(.exec_sample.count)",
    "  Mayaa 件数 : \(.exec_sample.mayaa_count)  (\(.exec_sample.mayaa_ratio)%)",
    "  ─ Mayaa スタック内トップクラス ─",
    (.exec_sample.mayaa_top_classes[] | "  \(.cnt)\t\(.cls)"),
    ""
  '
}

# 2ファイル差分比較
print_diff() {
  local a_json="$1"
  local b_json="$2"
  jq -rn \
    --argjson a "$a_json" \
    --argjson b "$b_json" \
    '
    def diff(x; y): (if y == 0 then "N/A" else (((y - x) / x * 1000 | round) / 10 | tostring) + "%" end);
    def arrow(x; y): if y > x then "▲" elif y < x then "▼" else "=" end;
    def fmt(v): v | (. * 100 | round) / 100 | tostring;

    "═══════════════════════════════════════════════════════════════",
    "  差分比較: A=[\($a.label | split("/") | last)]  vs  B=[\($b.label | split("/") | last)]",
    "  数値: A → B  (B-A の変化率; ▲=増加 ▼=減少)",
    "═══════════════════════════════════════════════════════════════",
    "",
    "■ JavaMonitorEnter",
    "  件数     : \($a.monitor.count) → \($b.monitor.count)  \(arrow($a.monitor.count; $b.monitor.count)) \(diff($a.monitor.count; $b.monitor.count))",
    "  合計ms   : \(fmt($a.monitor.total_ms)) → \(fmt($b.monitor.total_ms))  \(arrow($a.monitor.total_ms; $b.monitor.total_ms)) \(diff($a.monitor.total_ms; $b.monitor.total_ms))",
    "  avg ms   : \(fmt($a.monitor.avg_ms)) → \(fmt($b.monitor.avg_ms))  \(arrow($a.monitor.avg_ms; $b.monitor.avg_ms)) \(diff($a.monitor.avg_ms; $b.monitor.avg_ms))",
    "  p50 ms   : \(fmt($a.monitor.p50_ms)) → \(fmt($b.monitor.p50_ms))  \(arrow($a.monitor.p50_ms; $b.monitor.p50_ms)) \(diff($a.monitor.p50_ms; $b.monitor.p50_ms))",
    "  p90 ms   : \(fmt($a.monitor.p90_ms)) → \(fmt($b.monitor.p90_ms))  \(arrow($a.monitor.p90_ms; $b.monitor.p90_ms)) \(diff($a.monitor.p90_ms; $b.monitor.p90_ms))",
    "  p99 ms   : \(fmt($a.monitor.p99_ms)) → \(fmt($b.monitor.p99_ms))  \(arrow($a.monitor.p99_ms; $b.monitor.p99_ms)) \(diff($a.monitor.p99_ms; $b.monitor.p99_ms))",
    "  max ms   : \(fmt($a.monitor.max_ms)) → \(fmt($b.monitor.max_ms))  \(arrow($a.monitor.max_ms; $b.monitor.max_ms)) \(diff($a.monitor.max_ms; $b.monitor.max_ms))",
    "",
    "■ GarbageCollection",
    "  件数     : \($a.gc.count) → \($b.gc.count)  \(arrow($a.gc.count; $b.gc.count)) \(diff($a.gc.count; $b.gc.count))",
    "  合計ms   : \(fmt($a.gc.total_ms)) → \(fmt($b.gc.total_ms))  \(arrow($a.gc.total_ms; $b.gc.total_ms)) \(diff($a.gc.total_ms; $b.gc.total_ms))",
    "  avg ms   : \(fmt($a.gc.avg_ms)) → \(fmt($b.gc.avg_ms))  \(arrow($a.gc.avg_ms; $b.gc.avg_ms)) \(diff($a.gc.avg_ms; $b.gc.avg_ms))",
    "  max ms   : \(fmt($a.gc.max_ms)) → \(fmt($b.gc.max_ms))  \(arrow($a.gc.max_ms; $b.gc.max_ms)) \(diff($a.gc.max_ms; $b.gc.max_ms))",
    "",
    "■ ExecutionSample",
    "  総件数   : \($a.exec_sample.count) → \($b.exec_sample.count)  \(arrow($a.exec_sample.count; $b.exec_sample.count)) \(diff($a.exec_sample.count; $b.exec_sample.count))",
    "  Mayaa数  : \($a.exec_sample.mayaa_count) → \($b.exec_sample.mayaa_count)  \(arrow($a.exec_sample.mayaa_count; $b.exec_sample.mayaa_count)) \(diff($a.exec_sample.mayaa_count; $b.exec_sample.mayaa_count))",
    "  比率     : \($a.exec_sample.mayaa_ratio)% → \($b.exec_sample.mayaa_ratio)%",
    ""
  '
}

# ─── メイン ──────────────────────────────────────────────────────────────────

if [[ $# -eq 0 || $# -gt 2 ]]; then
  echo "Usage: $0 <jfr-file> [<jfr-file-baseline>]" >&2
  exit 1
fi

JFR_A="$1"
A_LABEL="$(basename "$JFR_A")"
echo "分析中: $JFR_A ..." >&2
A_JSON=$(analyze_jfr "$JFR_A" "$A_LABEL")
print_report "$A_JSON"

if [[ $# -eq 2 ]]; then
  JFR_B="$2"
  B_LABEL="$(basename "$JFR_B")"
  echo "分析中: $JFR_B ..." >&2
  B_JSON=$(analyze_jfr "$JFR_B" "$B_LABEL")
  print_report "$B_JSON"
  print_diff "$A_JSON" "$B_JSON"
fi
