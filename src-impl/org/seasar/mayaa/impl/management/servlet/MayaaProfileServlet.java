/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.management.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.engine.ProcessorDump;
import org.seasar.mayaa.impl.engine.RenderUtil;
import org.seasar.mayaa.impl.engine.processor.DirectiveProcessor;
import org.seasar.mayaa.impl.engine.processor.LiteralCharactersProcessor;
import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;
import org.seasar.mayaa.impl.management.SpecificationProfileRegistry;
import org.seasar.mayaa.impl.management.SpecificationStats;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.management.SpecificationProfileMXBean;

/**
 * Specification プロファイリングデータを JSON で提供する管理用 Servlet。
 *
 * <h2>エンドポイント一覧</h2>
 * <pre>
 * GET /&lt;mapping&gt;/summary                        全 Specification の集計 JSON
 * GET /&lt;mapping&gt;/top-slow?n=N                   avg render time 上位 N 件の JSON
 * GET /&lt;mapping&gt;/top-diag?n=N                   診断イベント上位 N 件の JSON
 * GET /&lt;mapping&gt;/spec?id=SYSTEM_ID              指定 Specification の JSON
 * GET /&lt;mapping&gt;/list-errors[?since=TS]         ERROR イベント全件 (差分対応)
 * GET /&lt;mapping&gt;/list-warnings[?since=TS]       WARN イベント全件 (差分対応)
 * GET /&lt;mapping&gt;/proc-dump?id=ID[&amp;format=text|json][&amp;contents=true] ProcessorDump
 * POST /&lt;mapping&gt;/reset                         全カウンタをリセット
 * POST /&lt;mapping&gt;/reset?id=SYSTEM_ID            指定 spec のカウンタをリセット
 * </pre>
 *
 * <h2>アクセス制御 (init-param)</h2>
 * <dl>
 *   <dt>{@code allowedCidr}</dt>
 *   <dd>
 *     許可する CIDR をカンマ区切りで指定する。
 *     未設定時はループバック ({@code 127.0.0.1/8,::1/128}) のみ許可。<br>
 *     例: {@code 127.0.0.1/8,10.0.0.0/8,192.168.0.0/16}
 *   </dd>
 * </dl>
 *
 * <p>本番環境では nginx/LB もしくは {@code web.xml} の
 * {@code &lt;security-constraint&gt;} で追加の認証を行うことを推奨する。</p>
 *
 * <h2>web.xml 設定例</h2>
 * <pre>{@code
 * <servlet>
 *   <servlet-name>mayaa-profile</servlet-name>
 *   <servlet-class>org.seasar.mayaa.impl.management.servlet.MayaaProfileServlet</servlet-class>
 *   <init-param>
 *     <param-name>allowedCidr</param-name>
 *     <param-value>127.0.0.1/8,10.0.0.0/8,192.168.0.0/16</param-value>
 *   </init-param>
 * </servlet>
 * <servlet-mapping>
 *   <servlet-name>mayaa-profile</servlet-name>
 *   <url-pattern>/mayaa-admin/profile/*</url-pattern>
 * </servlet-mapping>
 * }</pre>
 *
 * @since 2.0
 * @author Watanabe, Mitsutaka
 */
public class MayaaProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(MayaaProfileServlet.class);

    /** init-param 名: 許可 CIDR リスト (カンマ区切り) */
    public static final String PARAM_ALLOWED_CIDR = "allowedCidr";

    /** デフォルト許可範囲: ループバックのみ */
    private static final String DEFAULT_ALLOWED_CIDR = "127.0.0.0/8,::1/128";

    /** デフォルトの top-N 件数 */
    private static final int DEFAULT_TOP_N = 10;

    private final List<CidrBlock> _allowedBlocks = new ArrayList<>();

    // ----------------------------------------------------------------
    // ライフサイクル
    // ----------------------------------------------------------------

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String cidrParam = config.getInitParameter(PARAM_ALLOWED_CIDR);
        if (cidrParam == null || cidrParam.trim().isEmpty()) {
            cidrParam = DEFAULT_ALLOWED_CIDR;
        }
        for (String cidr : cidrParam.split(",")) {
            cidr = cidr.trim();
            if (!cidr.isEmpty()) {
                try {
                    _allowedBlocks.add(CidrBlock.parse(cidr));
                } catch (IllegalArgumentException e) {
                    LOG.warn("MayaaProfileServlet: invalid CIDR '" + cidr + "' (ignored)", e);
                }
            }
        }
        if (_allowedBlocks.isEmpty()) {
            LOG.warn("MayaaProfileServlet: no valid CIDR entries; defaulting to loopback only");
            _allowedBlocks.add(CidrBlock.parse("127.0.0.0/8"));
            _allowedBlocks.add(CidrBlock.parse("::1/128"));
        }
    }

    // ----------------------------------------------------------------
    // リクエスト処理
    // ----------------------------------------------------------------

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAllowed(req.getRemoteAddr())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access denied: remote address not in allowedCidr");
            return;
        }
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // pathInfo: /summary, /top-slow, /top-diag, /spec, /list-errors, /list-warnings
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            pathInfo = "/summary";
        }

        SpecificationProfileMXBean mxBean = SpecificationProfileRegistry.getInstance()
                .getMXBean();

        String json;
        switch (pathInfo) {
            case "/summary":
                json = mxBean.getSummaryAsJson();
                break;
            case "/top-slow":
                json = mxBean.getTopSlowRendersAsJson(getParamN(req));
                break;
            case "/top-diag":
                json = mxBean.getTopByDiagEventsAsJson(getParamN(req));
                break;
            case "/spec": {
                String id = req.getParameter("id");
                if (id == null || id.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Missing required parameter: id");
                    return;
                }
                json = mxBean.getSpecificationAsJson(id);
                break;
            }
            case "/spec-events": {
                String id = req.getParameter("id");
                if (id == null || id.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Missing required parameter: id");
                    return;
                }
                String kind = req.getParameter("kind"); // "errors", "warnings", or null (all)
                SpecificationStats specStats =
                        SpecificationProfileRegistry.getInstance().get(id);
                List<DiagnosticEventBuffer.Event> specEvts = specStats != null
                        ? specStats.snapshotEvents() : new ArrayList<>();
                if ("errors".equals(kind)) {
                    specEvts.removeIf(e -> e.level() != DiagnosticEventBuffer.Level.ERROR);
                } else if ("warnings".equals(kind)) {
                    specEvts.removeIf(e -> e.level() != DiagnosticEventBuffer.Level.WARN);
                }
                json = buildEventsJson(specEvts, 0L);
                break;
            }
            case "/list-errors": {
                long since = getParamSince(req);
                List<DiagnosticEventBuffer.Event> errs =
                        SpecificationProfileRegistry.getInstance().snapshotEventsSince(since);
                errs.removeIf(e -> e.level() != DiagnosticEventBuffer.Level.ERROR);
                json = buildEventsJson(errs, since);
                break;
            }
            case "/list-warnings": {
                long since = getParamSince(req);
                List<DiagnosticEventBuffer.Event> warns =
                        SpecificationProfileRegistry.getInstance().snapshotEventsSince(since);
                warns.removeIf(e -> e.level() != DiagnosticEventBuffer.Level.WARN);
                json = buildEventsJson(warns, since);
                break;
            }
            case "/proc-dump": {
                String id = req.getParameter("id");
                if (id == null || id.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Missing required parameter: id");
                    return;
                }
                boolean asJson = "json".equalsIgnoreCase(req.getParameter("format"));
                boolean showContents = "true".equalsIgnoreCase(req.getParameter("contents"));
                Specification spec = ProviderUtil.getEngine().findSpecificationFromCache(id);
                if (spec == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                            "Specification not found in cache: " + id);
                    return;
                }
                if (asJson) {
                    json = specDumpToJson(spec, showContents);
                } else {
                    String text = specDumpToText(spec, showContents);
                    resp.setContentType("text/plain;charset=UTF-8");
                    resp.setHeader("Cache-Control", "no-store");
                    resp.getWriter().write(text);
                    return;
                }
                break;
            }
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
                return;
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "/";

        SpecificationProfileMXBean mxBean = SpecificationProfileRegistry.getInstance()
                .getMXBean();

        switch (pathInfo) {
            case "/reset": {
                String id = req.getParameter("id");
                if (id != null && !id.isEmpty()) {
                    mxBean.reset(id);
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.setHeader("Cache-Control", "no-store");
                    resp.getWriter().write("{\"reset\":\"spec\",\"id\":" + quoteJson(id) + "}");
                } else {
                    mxBean.resetAll();
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.setHeader("Cache-Control", "no-store");
                    resp.getWriter().write("{\"reset\":\"all\"}");
                }
                break;
            }
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo);
        }
    }

    // ----------------------------------------------------------------
    // 内部ヘルパー
    // ----------------------------------------------------------------

    private boolean isAllowed(String remoteAddr) {
        if (remoteAddr == null) return false;
        InetAddress addr;
        try {
            addr = InetAddress.getByName(remoteAddr);
        } catch (UnknownHostException e) {
            return false;
        }
        for (CidrBlock block : _allowedBlocks) {
            if (block.contains(addr)) {
                return true;
            }
        }
        return false;
    }

    private static int getParamN(HttpServletRequest req) {
        String n = req.getParameter("n");
        if (n == null) return DEFAULT_TOP_N;
        try {
            int v = Integer.parseInt(n);
            return v > 0 ? v : DEFAULT_TOP_N;
        } catch (NumberFormatException e) {
            return DEFAULT_TOP_N;
        }
    }

    /**
     * クエリパラメータ {@code since} を long ミリ秒で返す。
     * 未指定・不正値の場合は 0 (バッファ全件) を返す。
     */
    private static long getParamSince(HttpServletRequest req) {
        String s = req.getParameter("since");
        if (s == null || s.trim().isEmpty()) return 0L;
        try {
            long v = Long.parseLong(s.trim());
            return v >= 0 ? v : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * {@link DiagnosticEventBuffer.Event} リストを
     * {@code {"nextSince":N,"count":N,"events":[...]}} 形式の JSON に変換する。
     *
     * <p>{@code nextSince} は返されたイベントのうち最大の {@code timestampMillis}。
     * イベントが空の場合は入力 {@code since} の値をそのまま返す。
     * 次回のポーリングで {@code ?since=nextSince} と指定することで差分取得できる。</p>
     */
    private static String buildEventsJson(
            List<DiagnosticEventBuffer.Event> events, long since) {
        long nextSince = since;
        StringBuilder sb = new StringBuilder();
        sb.append("[" );
        for (int i = 0; i < events.size(); i++) {
            DiagnosticEventBuffer.Event e = events.get(i);
            if (e.timestampMillis() > nextSince) nextSince = e.timestampMillis();
            if (i > 0) sb.append(",");
            sb.append(eventToJson(e));
        }
        sb.append("]");
        String eventsArr = sb.toString();
        return "{\"nextSince\":" + nextSince
                + ",\"count\":" + events.size()
                + ",\"events\":" + eventsArr + "}";
    }

    /** {@link DiagnosticEventBuffer.Event} を JSON オブジェクト文字列に変換する。 */
    private static String eventToJson(DiagnosticEventBuffer.Event e) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"ts\":").append(e.timestampMillis()).append(",");
        sb.append("\"phase\":").append(quoteJson(e.phase().name())).append(",");
        sb.append("\"level\":").append(quoteJson(e.level().name())).append(",");
        sb.append("\"label\":").append(quoteJson(e.label())).append(",");
        sb.append("\"source\":").append(quoteJson(e.source())).append(",");
        sb.append("\"message\":").append(quoteJson(e.message())).append(",");
        sb.append("\"scriptText\":").append(quoteJson(e.scriptText())).append(",");
        sb.append("\"sample\":").append(quoteJson(e.sample())).append(",");
        if (e.positionSystemID() != null
                || e.positionLineNumber() != DiagnosticEventBuffer.UNKNOWN_POSITION_LINE) {
            sb.append("\"position\":{");
            sb.append("\"systemID\":").append(quoteJson(e.positionSystemID())).append(",");
            sb.append("\"line\":").append(e.positionLineNumber()).append(",");
            sb.append("\"onTemplate\":").append(e.positionOnTemplate());
            sb.append("},");
        } else {
            sb.append("\"position\":null,");
        }
        sb.append("\"ownerSystemID\":").append(quoteJson(e.ownerSystemID())).append(",");
        sb.append("\"cause\":").append(e.cause() != null ? throwableToJson(e.cause()) : "null");
        sb.append("}");
        return sb.toString();
    }

    /**
     * {@link Throwable} を JSON オブジェクトに変換する。
     * cause チェーンを再帰的にシリアライズする。
     */
    private static String throwableToJson(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":").append(quoteJson(t.getClass().getName())).append(",");
        sb.append("\"message\":").append(quoteJson(t.getMessage())).append(",");
        sb.append("\"stackTrace\":[");
        StackTraceElement[] frames = t.getStackTrace();
        for (int i = 0; i < frames.length; i++) {
            if (i > 0) sb.append(",");
            StackTraceElement f = frames[i];
            sb.append("{");
            sb.append("\"class\":").append(quoteJson(f.getClassName())).append(",");
            sb.append("\"method\":").append(quoteJson(f.getMethodName())).append(",");
            sb.append("\"file\":").append(quoteJson(f.getFileName())).append(",");
            sb.append("\"line\":").append(f.getLineNumber());
            sb.append("}");
        }
        sb.append("]");
        if (t.getCause() != null) {
            sb.append(",\"cause\":").append(throwableToJson(t.getCause()));
        }
        sb.append("}");
        return sb.toString();
    }

    private static String quoteJson(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                        .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }

    // ================================================================
    // ProcessorDump ヘルパー
    // ================================================================

    /**
     * キャッシュ済みの {@link Specification} をテキスト形式でダンプする。
     * テンプレートの場合はプロセッサツリーとノードツリー、ページの場合はノードツリーを出力する。
     */
    private static String specDumpToText(Specification spec, boolean showContents) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            DumpCapture dump = new DumpCapture();
            dump.setOut(ps);
            dump.setPrintContents(showContents);
            if (spec instanceof Template) {
                Template template = (Template) spec;
                // printSource が SuperPage チェーンを含む全体を出力する
                dump.printSource(template.getPage(), template.getSuffix(), template.getExtension());
            } else if (spec instanceof Page) {
                Page page = (Page) spec;
                ps.println("SYSTEM-ID: " + page.getSystemID());
                ps.println("TYPE: page");
                ps.println("PAGE NODE TREE --------------------------------");
                dump.dumpNodeTree(page);
            }
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    /**
     * キャッシュ済みの {@link Specification} を JSON 形式でダンプする。
     * AI ツールによる解析を想定した構造化出力。
     */
    private static String specDumpToJson(Specification spec, boolean showContents) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"systemID\":").append(quoteJson(spec.getSystemID())).append(",");
        if (spec instanceof Template) {
            Template template = (Template) spec;
            sb.append("\"type\":\"template\",");
            sb.append("\"processorTree\":").append(procNodeToJson(template, showContents, 0)).append(",");
            sb.append("\"nodeTree\":").append(specNodeToJson(spec, showContents, 0)).append(",");
            sb.append("\"pageNodeTree\":").append(specNodeToJson(template.getPage(), showContents, 0)).append(",");
            sb.append("\"layoutChain\":[");
            appendLayoutChainJson(sb, template.getPage(), template.getSuffix(), template.getExtension(), showContents);
            sb.append("]");
        } else if (spec instanceof Page) {
            sb.append("\"type\":\"page\",");
            sb.append("\"nodeTree\":").append(specNodeToJson(spec, showContents, 0)).append(",");
            sb.append("\"layoutChain\":[");
            appendLayoutChainJson(sb, (Page) spec, null, null, showContents);
            sb.append("]");
        } else {
            sb.append("\"type\":\"specification\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * レイアウトチェーンを追い、各レイアウトテンプレートの JSON エントリを sb に追加する。
     * {@link ProcessorDump#printSource(Page, String, String)} と同じ SuperPage 解決ロジックを使用する。
     */
    private static void appendLayoutChainJson(StringBuilder sb, Page startPage,
            String requestedSuffix, String extension, boolean showContents) {
        Page page = startPage;
        boolean first = true;
        while (true) {
            Page superPage = page.getSuperPage();
            if (superPage == null) break;
            String superSuffix = page.getSuperSuffix();
            String superExtension = page.getSuperExtension();
            Template layoutTemplate = RenderUtil.getTemplate(
                    requestedSuffix, superPage, superSuffix, superExtension);
            if (layoutTemplate != null) {
                if (!first) sb.append(",");
                sb.append("{");
                sb.append("\"systemID\":").append(quoteJson(layoutTemplate.getSystemID())).append(",");
                sb.append("\"processorTree\":").append(procNodeToJson(layoutTemplate, showContents, 0)).append(",");
                sb.append("\"nodeTree\":").append(specNodeToJson(layoutTemplate, showContents, 0)).append(",");
                sb.append("\"pageNodeTree\":").append(specNodeToJson(superPage, showContents, 0));
                sb.append("}");
                first = false;
            }
            page = superPage;
        }
    }

    private static String procNodeToJson(ProcessorTreeWalker walker, boolean showContents, int depth) {
        if (depth > 200) return "{\"truncated\":true}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (walker instanceof TemplateProcessor) {
            TemplateProcessor tp = (TemplateProcessor) walker;
            SpecificationNode injected = tp.getInjectedNode();
            SpecificationNode original = tp.getOriginalNode();
            sb.append("\"class\":").append(quoteJson(walker.getClass().getSimpleName())).append(",");
            sb.append("\"systemID\":").append(quoteJson(injected.getSystemID())).append(",");
            sb.append("\"line\":").append(injected.getLineNumber()).append(",");
            if (walker instanceof DirectiveProcessor) {
                DirectiveProcessor dp = (DirectiveProcessor) walker;
                sb.append("\"directive\":").append(quoteJson(dp.getDirectiveName())).append(",");
                sb.append("\"value\":").append(quoteJson(dp.getDirectiveValue())).append(",");
            } else if (walker instanceof LiteralCharactersProcessor) {
                String text = ((LiteralCharactersProcessor) walker).getText();
                if (showContents) {
                    sb.append("\"text\":").append(quoteJson(text)).append(",");
                } else {
                    sb.append("\"textLength\":").append(text != null ? text.length() : 0).append(",");
                }
            } else {
                QName qname = injected.getQName();
                sb.append("\"qname\":").append(quoteJson(qname.getLocalName())).append(",");
                String nsUri = qname.getNamespaceURI() != null ? qname.getNamespaceURI().toString() : null;
                sb.append("\"ns\":").append(quoteJson(nsUri)).append(",");
            }
            if (original.getSequenceID() != injected.getSequenceID()) {
                sb.append("\"originalSystemID\":").append(quoteJson(original.getSystemID())).append(",");
                sb.append("\"originalLine\":").append(original.getLineNumber()).append(",");
            }
        } else {
            sb.append("\"class\":").append(quoteJson(walker.getClass().getSimpleName())).append(",");
        }
        int childSize = walker.getChildProcessorSize();
        sb.append("\"children\":[");
        for (int i = 0; i < childSize; i++) {
            if (i > 0) sb.append(",");
            sb.append(procNodeToJson(walker.getChildProcessor(i), showContents, depth + 1));
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private static String specNodeToJson(NodeTreeWalker walker, boolean showContents, int depth) {
        if (depth > 200) return "{\"truncated\":true}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (walker instanceof SpecificationNode) {
            SpecificationNode node = (SpecificationNode) walker;
            QName qname = node.getQName();
            sb.append("\"qname\":").append(quoteJson(qname.getLocalName())).append(",");
            String nsUri = qname.getNamespaceURI() != null ? qname.getNamespaceURI().toString() : null;
            sb.append("\"ns\":").append(quoteJson(nsUri)).append(",");
            sb.append("\"systemID\":").append(quoteJson(node.getSystemID())).append(",");
            sb.append("\"line\":").append(node.getLineNumber()).append(",");
            sb.append("\"attributes\":[");
            Iterator<NodeAttribute> attrs = node.iterateAttribute();
            boolean firstAttr = true;
            while (attrs.hasNext()) {
                NodeAttribute attr = attrs.next();
                if (!firstAttr) sb.append(",");
                sb.append("{\"name\":").append(quoteJson(attr.getQName().getLocalName()));
                sb.append(",\"value\":").append(quoteJson(attr.getValue()));
                sb.append("}");
                firstAttr = false;
            }
            sb.append("],");
        } else {
            sb.append("\"class\":").append(quoteJson(walker.getClass().getSimpleName())).append(",");
        }
        int childSize = walker.getChildNodeSize();
        sb.append("\"children\":[");
        for (int i = 0; i < childSize; i++) {
            if (i > 0) sb.append(",");
            sb.append(specNodeToJson(walker.getChildNode(i), showContents, depth + 1));
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    /** ProcessorDump の protected ツリー走査を公開するためのサブクラス。 */
    private static class DumpCapture extends ProcessorDump {
        void dumpProcessorTree(ProcessorTreeWalker walker) {
            printProcessorTree(0, walker);
        }
        void dumpNodeTree(NodeTreeWalker walker) {
            printSpecificationNodeTree(0, walker);
        }
    }

    // ================================================================
    // CIDR チェック (追加ライブラリ不要)
    // ================================================================

    /**
     * CIDR ブロック (IPv4 / IPv6 両対応)。
     * {@link InetAddress#getByName} を使って変換し、ビットマスクで比較する。
     */
    static final class CidrBlock {

        private final byte[] _networkAddr;
        private final int    _prefixLen;

        private CidrBlock(byte[] networkAddr, int prefixLen) {
            _networkAddr = networkAddr;
            _prefixLen   = prefixLen;
        }

        /**
         * "a.b.c.d/n" または "::1/128" 形式の文字列をパースする。
         *
         * @throws IllegalArgumentException 形式不正または解決不能な場合
         */
        static CidrBlock parse(String cidr) {
            int slash = cidr.lastIndexOf('/');
            if (slash < 0) {
                throw new IllegalArgumentException("No prefix length in CIDR: " + cidr);
            }
            String addrPart   = cidr.substring(0, slash);
            String prefixPart = cidr.substring(slash + 1);
            int prefixLen;
            try {
                prefixLen = Integer.parseInt(prefixPart);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid prefix length: " + prefixPart);
            }
            InetAddress addr;
            try {
                addr = InetAddress.getByName(addrPart);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Unresolvable address: " + addrPart, e);
            }
            byte[] rawAddr = addr.getAddress();
            int maxLen = rawAddr.length * 8;
            if (prefixLen < 0 || prefixLen > maxLen) {
                throw new IllegalArgumentException(
                        "Prefix length out of range for address family: " + prefixLen);
            }
            return new CidrBlock(maskAddress(rawAddr, prefixLen), prefixLen);
        }

        boolean contains(InetAddress candidate) {
            byte[] raw = candidate.getAddress();
            if (raw.length != _networkAddr.length) {
                // IPv4/IPv6 の混在への対応: IPv4-mapped IPv6 を展開するため
                // 同ファミリーでない場合は不一致とする。
                return false;
            }
            return arrayEquals(maskAddress(raw, _prefixLen), _networkAddr);
        }

        private static byte[] maskAddress(byte[] addr, int prefixLen) {
            byte[] masked = addr.clone();
            int fullBytes = prefixLen / 8;
            int remainBits = prefixLen % 8;
            for (int i = fullBytes; i < masked.length; i++) {
                if (i == fullBytes && remainBits > 0) {
                    masked[i] = (byte) (masked[i] & (0xFF << (8 - remainBits)));
                } else {
                    masked[i] = 0;
                }
            }
            return masked;
        }

        private static boolean arrayEquals(byte[] a, byte[] b) {
            if (a.length != b.length) return false;
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) return false;
            }
            return true;
        }
    }
}
