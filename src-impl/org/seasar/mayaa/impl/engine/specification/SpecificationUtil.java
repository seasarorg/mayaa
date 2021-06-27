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
package org.seasar.mayaa.impl.engine.specification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.LiteralScript;
import org.seasar.mayaa.impl.cycle.script.ScriptUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * Specificationに関わるユーティリティメソッドをまとめたクラスです。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationUtil implements CONST_IMPL {

    private static final Log LOG = LogFactory.getLog(SpecificationUtil.class);

    private static EventScriptEnvironment _eventScripts =
        new EventScriptEnvironment();

    public static final PrefixMapping XML_DEFAULT_PREFIX_MAPPING =
        PrefixMappingImpl.getInstance("xml", CONST_IMPL.URI_XML);

    public static final PrefixMapping HTML_DEFAULT_PREFIX_MAPPING =
        PrefixMappingImpl.getInstance("", CONST_IMPL.URI_HTML);

    public static final PrefixMapping XHTML_DEFAULT_PREFIX_MAPPING =
        PrefixMappingImpl.getInstance("", CONST_IMPL.URI_XHTML);

    public static File _serializeDirFile;

    private SpecificationUtil() {
        // no instantiation.
    }

    /**
     * nodeの属性のうちqNameで表されるものの値を返します。
     * 見つからない場合はnullを返します。
     *
     * @param node 対象とするノード
     * @param qName 属性名
     * @return 属性値
     */
    public static String getAttributeValue(
            SpecificationNode node, QName qName) {
        NodeAttribute nameAttr = node.getAttribute(qName);
        if (nameAttr != null) {
            return nameAttr.getValue();
        }
        return null;
    }

    /**
     * currentのparentを辿り、Specificationが見つかった場合それを返します。
     * 見つからない場合はnullを返します。
     *
     * @param current 対象とするノード
     * @return Specification
     */
    public static Specification findSpecification(NodeTreeWalker current) {
        while (current instanceof Specification == false) {
            current = current.getParentNode();
            if (current == null) {
                return null;
            }
        }
        return (Specification) current;
    }

    /**
     * ServiceCycleで現在処理中のノードからparentを辿り、Specificationが
     * 見つかった場合それを返します。
     * 見つからない場合はnullを返します。
     *
     * @return Specification
     */
    public static Specification findSpecification() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        NodeTreeWalker current = cycle.getOriginalNode();
        // リソースアクセス中にredirectしても対応できるようにする。
        if (current != null) {
            return findSpecification(current);
        }
        return null;
    }

    /**
     * currentが持つm:mayaaノードを探し、もしあればm:mayaaノードの属性のうち
     * qNameで表されるものの値を返します。
     * 見つからない場合はnullを返します。
     *
     * @param current 対象とするノード
     * @return 属性値
     */
    public static SpecificationNode getMayaaNode(NodeTreeWalker current) {
        Specification specification = findSpecification(current);
        for (Iterator<NodeTreeWalker> it = specification.iterateChildNode(); it.hasNext();) {
            SpecificationNode node = (SpecificationNode) it.next();
            if (node.getQName().equals(QM_MAYAA)) {
                return node;
            }
        }
        return null;
    }

    /**
     * currentが持つm:mayaaノードを探し、もしあればm:mayaaノードの属性のうち
     * qNameで表されるものの値を返します。
     * 見つからない場合はnullを返します。
     *
     * @param current 対象とするノード
     * @param qName 属性名
     * @return 属性値
     */
    public static String getMayaaAttributeValue(
            NodeTreeWalker current, QName qName) {
        SpecificationNode mayaa = getMayaaNode(current);
        if (mayaa != null) {
            String value = getAttributeValue(mayaa, qName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * nodeの子要素である文字ノードをまとめた文字列を取得します。
     * 文字ノード以外を含む場合、{@link IllegalChildNodeException} が発生します。
     * CDATAはその子を文字ノードと見なします。
     *
     * @param node 文字ノードを子として持つノード
     * @return 文字ノードをまとめた文字列
     * @throws IllegalChildNodeException 文字ノード以外を含む場合
     */
    public static String getNodeBodyText(SpecificationNode node) {
        StringBuilder buffer = new StringBuilder(256);
        for (Iterator<NodeTreeWalker> it = node.iterateChildNode(); it.hasNext();) {
            SpecificationNode child = (SpecificationNode) it.next();
            QName qName = child.getQName();
            if (QM_CDATA.equals(qName)) {
                buffer.append(getNodeBodyText(child));
            } else if (QM_CHARACTERS.equals(qName)) {
                buffer.append(SpecificationUtil.getAttributeValue(
                        child, QM_TEXT));
            } else {
                String name = child.getPrefix() + ":" + qName.getLocalName();
                throw new IllegalChildNodeException(name);
            }
        }
        return buffer.toString();
    }

    /**
     * スクリプト実行環境のスコープを初期化します。
     */
    public static void initScope() {
        ProviderUtil.getScriptEnvironment().initScope();
    }

    /**
     * スクリプト実行環境の新規スコープを開始します。
     * @param variables 初期変数
     */
    public static void startScope(Map<?, ?> variables) {
        ProviderUtil.getScriptEnvironment().startScope(variables);
    }

    /**
     * スクリプト実行環境の現在スコープを終了します。
     */
    public static void endScope() {
        ProviderUtil.getScriptEnvironment().endScope();
    }

    public static void execEvent(Specification spec, QName eventName) {
        if (eventName == null) {
            throw new IllegalArgumentException();
        }
        SpecificationNode mayaa = getMayaaNode(spec);
        if (mayaa != null) {
        	if (_eventScripts.isCached(spec, eventName)) {
        		_eventScripts.execEventScripts(spec, eventName);
        	} else {
        		List<NodeTreeWalker> eventChilds = new ArrayList<>();
	            for (Iterator<NodeTreeWalker> it = mayaa.iterateChildNode(); it.hasNext();) {
	                SpecificationNode child = (SpecificationNode) it.next();
	                if (eventName.equals(child.getQName())) {
	                	eventChilds.add(child);
	                }
	            }
        		_eventScripts.execEventScriptsAndCache(spec, eventName, eventChilds);
        	}
        }
    }

    // factory methods ----------------------------------------------

    public static Namespace createNamespace() {
        return new NamespaceImpl();
    }

    public static Namespace getFixedNamespace(Namespace original) {
        return original;
        //return NamespaceImpl.getInstance(original);
    }

    public static QName createQName(String localName) {
        return createQName(URI_MAYAA, localName);
    }

    public static QName createQName(
            URI namespaceURI, String localName) {
        return QNameImpl.getInstance(namespaceURI, localName);
    }

    public static PrefixMapping createPrefixMapping(
            String prefix, URI namespaceURI) {
        return PrefixMappingImpl.getInstance(prefix, namespaceURI);
    }

    public static PrefixMapping createPrefixMapping(
            String prefixAndNamespaceURI) {
        return PrefixMappingImpl.revertStringToMapping(prefixAndNamespaceURI);
    }

    public static QName parseQName(String qName) {
        if (StringUtil.hasValue(qName) && qName.charAt(0) == '{') {
            int end = qName.indexOf('}');
            if (end != -1 && end < qName.length() - 1) {
                String namespaceURI = qName.substring(1, end).trim();
                String localName = qName.substring(end + 1).trim();
                if (StringUtil.hasValue(namespaceURI)
                        && StringUtil.hasValue(localName)) {
                    return QNameImpl.getInstance(createURI(namespaceURI), localName);
                }
            }
        }
        throw new IllegalArgumentException(qName);
    }

    public static PrefixAwareName createPrefixAwareName(QName qName) {
        return createPrefixAwareName(qName, "");
    }

    public static PrefixAwareName createPrefixAwareName(QName qName, String prefix) {
        return PrefixAwareNameImpl.getInstance(qName, prefix);
    }

    public static SpecificationNode createSpecificationNode(
            QName qName, String systemID, int lineNumber,
            boolean onTemplate, int sequenceID) {
        SpecificationNodeImpl node = new SpecificationNodeImpl(qName);
        node.setSequenceID(sequenceID);
        node.setSystemID(systemID);
        node.setLineNumber(lineNumber);
        node.setOnTemplate(onTemplate);
        return node;
    }

    public static URI createURI(String uri) {
        return URIImpl.getInstance(uri);
    }

    /**
     * {@link Specification} のシリアライズを有効にするための準備を行う。
     * シリアライズ後のファイル保管ディレクトリが作成できないなどの理由で失敗した場合はfalseを返す。
     * @return シリアライズを有効にするための準備が整わないときに false を返す。
     */
    public static boolean prepareSerialize() {
        try {
            ApplicationScope scope = CycleUtil.getServiceCycle().getApplicationScope();
            ServletContext context = (ServletContext) scope.getUnderlyingContext();
            File baseDir = (File) context.getAttribute("javax.servlet.context.tempdir");
            if (baseDir == null || !baseDir.exists()) {
                return false; // cannot resolve spec cache directory.
            }
            File cacheDir = new File(baseDir, ".mayaaSpecCache");
            if (cacheDir.exists() || cacheDir.mkdirs()) {
                _serializeDirFile = cacheDir;
            }
            else {
                LOG.error("Cannot mkdir serialize directory. " + cacheDir.getAbsolutePath());
                return false;
            }
        } catch (SecurityException e) {
            return false;
        }

        // すべて問題がなければ true
        return true;
    }

    public static void cleanupSerialize() {
        if (_serializeDirFile != null) {
            try {
                Files.deleteIfExists(_serializeDirFile.toPath());
            } catch (IOException e) {
            }
        }
    }

    /**
     * キャッシュ用にシリアライズするディレクトリのファイルオブジェクトを取得する。
     * @return Fileオブジェクト
     */
    protected static File getSerializeDirectory() {
        if (_serializeDirFile == null || !_serializeDirFile.exists()) {
            prepareSerialize();
        }

        return _serializeDirFile;
    }

    /**
     * システムID指定したシリアライズされたファイルを削除する。
     * 存在していない場合も失敗しない。
     * @param systemID システムID
     */
    public static void purgeSerializedFile(String systemID) {
        File cacheDir = getSerializeDirectory();
        String filename = getSerializedFilename(systemID);
        File cacheFile = new File(cacheDir, filename);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }

    /**
     * システムIDからキャッシュ用にシリアライズするファイル名を生成する。
     * @param systemID システムID
     * @return シリアライズ用ファイル名
     */
    protected static String getSerializedFilename(String systemID) {
        return systemID.substring("/".length()).replace('/', '#') + ".ser";
    }

    public static void serialize(Specification spec) {
        File cacheDir = getSerializeDirectory();
        serialize(spec, cacheDir);
    }

    public static void serialize(Specification spec, File cacheDir) {
        try {
            String filename = getSerializedFilename(spec.getSystemID());

            synchronized(spec) {
                File file = new File(cacheDir, filename);
                try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))){
                    stream.writeObject(spec);
                }
            }
        } catch (FileNotFoundException e) {
            LOG.error("page serialize failed.", e);
        } catch (IOException e) {
            LOG.error("page serialize failed.", e);
        } catch (IllegalStateException e) {
            LOG.error("page serialize failed.", e);
        }
    }

    public static Specification deserialize(String systemID) {
        File cacheDir = getSerializeDirectory();
        return deserialize(systemID, cacheDir);
    }

    public static Specification deserialize(String systemID, File cacheDir) {
        File cacheFile = null;
        try {
            String filename = getSerializedFilename(systemID);
            cacheFile = new File(cacheDir, filename);
            if (cacheFile.exists() == false) {
                return null;
            }
        } catch (IllegalStateException e) {
            return null;
        }


        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(cacheFile))) {
            Specification result = (Specification) stream.readObject();
            return result;
        } catch(Throwable e) {
            String message = systemID + " specification deserialize failed.";
            if (e.getMessage() != null) {
                message += " " + e.getMessage();
            }
            LOG.info(message);
            cacheFile.delete();
            return null;
        }
    }

    // script cache ----------------------------------------------

    protected static class EventScriptEnvironment {

		// WeakHashMap<Specification, Map<QName, List<CompiledScript>>>
        private Map<Specification, Map<QName, List<CompiledScript>>> _mayaaScriptCache
             = Collections.synchronizedMap(new WeakHashMap<Specification, Map<QName, List<CompiledScript>>>());

        protected CompiledScript compile(String text) {
            if (StringUtil.hasValue(text)) {
                ScriptUtil.assertSingleScript(text);
                return ScriptUtil.compile(text, Void.class);
            }
            return LiteralScript.NULL_LITERAL_SCRIPT;
        }

        protected List<CompiledScript> getEventScripts(Specification spec, QName eventName) {
        	Map<QName, List<CompiledScript>> events = _mayaaScriptCache.get(spec);
        	if (events == null) {
        		return null;
        	}
        	return events.get(eventName);
        }

        public boolean isCached(Specification spec, QName eventName) {
        	return getEventScripts(spec, eventName) != null;
        }

        public boolean execEventScriptsAndCache(
        		Specification spec, QName eventName, List<NodeTreeWalker> eventChilds) {
        	if (eventChilds == null) {
        		throw new NullPointerException();
        	}
        	Map<QName, List<CompiledScript>> events = _mayaaScriptCache.get(spec);
        	if (events == null) {
        		events = new HashMap<>();
        		_mayaaScriptCache.put(spec, events);
        	}
        	List<CompiledScript> scripts = new ArrayList<>();

            ServiceCycle cycle = CycleUtil.getServiceCycle();
            for (Iterator<NodeTreeWalker> it = eventChilds.iterator(); it.hasNext();) {
                SpecificationNode child = (SpecificationNode) it.next();
                NodeTreeWalker save = cycle.getInjectedNode();
                try {
                    cycle.setInjectedNode(child);
                    String bodyText = getNodeBodyText(child);
                    bodyText = ScriptUtil.getBlockSignedText(bodyText);
                    CompiledScript script = compile(bodyText);
                    scripts.add(script);
                    script.execute(null);
                } finally {
                    cycle.setInjectedNode(save);
                }
            }
            if (scripts.size() == 0) {
            	events.put(eventName, new ArrayList<CompiledScript>());
            } else {
            	events.put(eventName, scripts);
            }
        	return true;
        }

        public boolean execEventScripts(
        		Specification spec, QName eventName) {
        	List<CompiledScript> scripts = getEventScripts(spec, eventName);
        	if (scripts == null) {
        		return false;
        	}
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            for (CompiledScript script : scripts) {
                NodeTreeWalker save = cycle.getInjectedNode();
                try {
                    script.execute(null);
                } finally {
                    cycle.setInjectedNode(save);
                }
            }
            return true;
        }

    }

    public static Namespace copyNamespace(Namespace original) {
        return NamespaceImpl.copyOf(original);
    }

    public static URI getDefaultTemplateNamespace(String systemID) {
        return null;
    }

    /**
     * デフォルトのSpecificationをソースビルドを行ってから返す。
     * @return デフォルトのSpecification
     */
    public static Specification getDefaultSpecification() {
        return (Specification) CycleUtil.getGlobalVariable(CONST_IMPL.DEFAULT_SPECIFICATION_KEY, null);
    }
}
