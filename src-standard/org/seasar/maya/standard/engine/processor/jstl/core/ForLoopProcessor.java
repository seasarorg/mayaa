package org.seasar.maya.standard.engine.processor.jstl.core;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.IterationTag;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;

/**
 * @author maruo_syunsuke
 */
public class ForLoopProcessor extends TemplateProcessorSupport implements IterationProcessor {

	// ネストに気をつける
	private static final int DEFAULT_STEP_START_VALUE = 1;
    private static final int DEFAULT_INDEX_START_VALUE = 0;
    
    private ProcessorProperty	_varParameter 	= null ;
    private ProcessorProperty	_beginParameter = null ;
    private ProcessorProperty	_endParameter	= null ;
    private ProcessorProperty	_stepParameter	= null ;
    
    private static final String LOCAL_INDEX = "LOCAL_INDEX" ;
    private static final String LOCAL_VAR   = "LOCAL_VAR" ;
    private static final String LOCAL_STEP  = "LOCAL_STEP" ;
    private static final String LOCAL_START = "LOCAL_START" ;
    private static final String LOCAL_END   = "LOCAL_END" ;
    
    protected boolean nextArrayItem(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        
        if( getEndValue(context) >= getIndexValue(context) ) {
	        getVarAttribute(context).setValue(context, getCurrentItem(context));
	        setIndexValue( context, getIndexValue(context) + getStepValue(context) );
	        return true;
        }
        getVarAttribute(context).remove(context);
        return false;
    }
    protected Object getCurrentItem(PageContext context){
        return new Integer(getIndexValue(context));
    }
    private String getKey() {
        return getClass().getName() + "@" + hashCode();
    }
    
    public int doStartProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }

        initParameter(context);
        return nextArrayItem(context) ? Tag.EVAL_BODY_INCLUDE : Tag.SKIP_BODY;
    }

	private void initParameter(PageContext context) {
	    AttributeValue varAttribute = AttributeValueFactory.create( (String)_varParameter.getValue(context));
        setVarAttribute(context, varAttribute);
        setIndexValue( context, initBeginParameter(context) );
	    setStepValue( context, initStepParameter(context) );
        setEndValue( context, initEndParameter(context) );
        
        varAttribute.setValue(context,new Integer( getIndexValue(context) ));
	}

	protected int initBeginParameter(PageContext context) {
        if( _beginParameter != null ){
		    Object value = _beginParameter.getValue(context);
			if( value != null ){
		        if( value instanceof Integer ){
		            Integer beginValue = (Integer)value ;
			    	return beginValue.intValue() ;
				}else if( value != null && value instanceof String ){
					return Integer.valueOf((String)value).intValue() ;
				}
			}
	    }
        return DEFAULT_INDEX_START_VALUE;
    }
	protected int initStepParameter(PageContext context) {
        if( _stepParameter != null ){
		    Object value = _stepParameter.getValue(context);
			if( value != null && value instanceof Integer ){
			    Integer stepValue = (Integer)value ;
			    return stepValue.intValue() ;
			}else if( value != null && value instanceof String ){
			    return Integer.valueOf((String)value).intValue() ;
			}
	    }
        return DEFAULT_STEP_START_VALUE;
    }
	protected int initEndParameter(PageContext context) {
        Integer endValue = getEndParameterValue(context);
        if( endValue != null ){
            return endValue.intValue();
        }
        return 0 ;
    }

	protected Integer getEndParameterValue(PageContext context) {
        Integer endValue = null ;
        if( _endParameter != null ){
		    Object value = _endParameter.getValue(context);
			if( value != null ){
			    if( value instanceof Integer ){
			        endValue = (Integer)value ;
				}else if( value instanceof String ){
				    endValue = new Integer((String)value );
				}
	 		}
 		}
        return endValue;
    }

    public int doAfterChildProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        return nextArrayItem(context) ? IterationTag.EVAL_BODY_AGAIN : Tag.SKIP_BODY;
    }
    
    public int doEndProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        context.removeAttribute(getKey());
        return Tag.EVAL_PAGE;
    }

    public boolean isIteration(PageContext context) {
        return true;
    }
    
    // Local Value
    protected int getIntValue(String name,PageContext context){
        Integer indexObject = (Integer)ProcessorLocalValueUtil.getObject(context,this,name);
        return indexObject.intValue();
    }
    protected void setIntValue(String name, PageContext context,int value){
        ProcessorLocalValueUtil.setObject(context,this,name,new Integer(value));
    }
    protected int getIndexValue(PageContext context){
        return getIntValue(LOCAL_INDEX,context);
    }
    protected void setIndexValue(PageContext context,int index){
        setIntValue(LOCAL_INDEX,context,index);
    }
    protected int getStepValue(PageContext context){
        return getIntValue(LOCAL_STEP,context);
    }
    protected void setStepValue(PageContext context,int step){
        setIntValue(LOCAL_STEP,context,step);
    }
    protected int getSartValue(PageContext context){
        return getIntValue(LOCAL_START,context);
    }
    protected void setStartValue(PageContext context,int step){
        setIntValue(LOCAL_START,context,step);
    }
    protected int getEndValue(PageContext context){
        return getIntValue(LOCAL_END,context);
    }
    protected void setEndValue(PageContext context,int step){
        setIntValue(LOCAL_END,context,step);
    }
    protected AttributeValue getVarAttribute(PageContext context){
        String varName = (String)ProcessorLocalValueUtil.getObject(context,this,LOCAL_VAR);
        return AttributeValueFactory.create(varName);
    }
    protected void setVarAttribute(PageContext context,AttributeValue value){
        ProcessorLocalValueUtil.setObject(context,this,LOCAL_VAR,value.getName());
    }
    
    ////
    public void setBeginParameter(ProcessorProperty beginParameter) {
        _beginParameter = beginParameter;
    }
    public void setEndParameter(ProcessorProperty endParameter) {
        _endParameter = endParameter;
    }
    public void setStepParameter(ProcessorProperty stepParameter) {
        _stepParameter = stepParameter;
    }
    public void setVarParameter(ProcessorProperty varParameter) {
        _varParameter = varParameter;
    }
}

