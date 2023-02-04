package org.seasar.mayaa.regressions.issue13;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.processor.TemplateProcessorSupport;

/**
 * 
 * [mayaa-user:1087] 独自プロセッサーを使うとエラーが発生する場合がある
 * #13 https://github.com/seasarorg/mayaa/issues/13 の再現用のカスタムプロセッサ
 */
public class FormatDateProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -2331626109260967664L;

    private ProcessorProperty _value;
    private String _pattern;

    @Override
    public void initialize() {
        if (_pattern == null) {
            _pattern = new SimpleDateFormat().toPattern();
        }
    }

    // MLD property, expectedClass=java.time.LocalDateTime
    public void setValue(ProcessorProperty value) {
        this._value = value;
    }

    public ProcessorProperty getValue() {
        return _value;
    }

    public void setPattern(String pattern) {
        _pattern = pattern;
    }

    @Override
    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (_value != null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(format(_value));
        }
        return ProcessStatus.SKIP_BODY;
    }

    private String format(ProcessorProperty property) {
        Object result = property.getValue().execute(null);
        if (result != null) {
            if (result instanceof LocalDateTime) {
                LocalDateTime dt = (LocalDateTime) result;
                return dt.format(DateTimeFormatter.ISO_LOCAL_DATE);
            }

            throw new IllegalArgumentException(
                    "argument type mismatch: " + result.getClass().getName());
        }
        return "";
    }
}
