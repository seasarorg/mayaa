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
    
    private int _index ;
    private int _step  ;
    private int _end   ;
	
    // tag status params 
    private AttributeValue  _var		= null ;
    
    private ProcessorProperty	_varParameter 		= null ;// OPTIONAL
    private ProcessorProperty	_beginParameter 	= null ;// OPTIONAL
    private ProcessorProperty	_endParameter		= null ;// OPTIONAL
    private ProcessorProperty	_stepParameter		= null ;// OPTIONAL
    
    protected boolean nextArrayItem(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }

        if( _end >= _index ) {
	        setCurrentObjectToVarValue(context);
	        _index += _step ;
	        return true;
        }
        _var.remove(context);
        return false;
    }

    protected void setCurrentObjectToVarValue(PageContext context) {
	    setVarValue(context,new Integer( _index ));
	}

    protected void setVarValue(PageContext context, Object value) {
        _var.setValue( context,value );
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
	    _var   = AttributeValueFactory.create( (String)_varParameter.getValue(context));
	    _index = initBeginParameter(context);
	    _step  = initStepParameter(context);
	    _end   = initEndParameter(context);
        
	    setVarValue(context,new Integer( _index ));
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
    
    protected int getIndexValue(){
        return _index;
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
    public void setVar(AttributeValue var) {
        _var = var;
    }
    public void setVarParameter(ProcessorProperty varParameter) {
        _varParameter = varParameter;
    }
}

