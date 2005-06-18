package org.seasar.maya.sample.customtag.tei;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;


public class CsvIteratorAtEndTei extends TagExtraInfo {
    public VariableInfo[] getVariableInfo(TagData tagData) {
        VariableInfo[] variableInfos = new VariableInfo[] {
            new VariableInfo(
                tagData.getId(),
                "org.seasar.maya.sample.customtag.tei.IteratorHolder",
                true,
                VariableInfo.AT_END)
        };
        return variableInfos;
    }
}
