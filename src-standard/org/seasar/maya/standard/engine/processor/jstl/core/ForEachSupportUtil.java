package org.seasar.maya.standard.engine.processor.jstl.core;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author maruo_syunsuke
 */
public class ForEachSupportUtil{
	public static ReadOnlyList toForEachList(final boolean[] booleanArray){
		return new ReadOnlyList() {
			public Object get(int index) {
				return new Boolean( booleanArray[index] );
			}

			public int size() {
				return booleanArray.length ;
			}
		}; 
	}

	public static ReadOnlyList toForEachList(final byte[] byteArray){
		return new ReadOnlyList() {
			public Object get(int index) {
				return new Byte( byteArray[index] );
			}

			public int size() {
				return byteArray.length ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(final char[] charArray){
		return new ReadOnlyList() {
			public Object get(int index) {
				return new Character( charArray[index] );
			}
			public int size() {
				return charArray.length ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(java.util.Collection collectionObject){
		final Object[] objectArray = collectionObject.toArray() ;
		return new ReadOnlyList() {
			public Object get(int index) {
				return objectArray[index];
			}
			public int size() {
				return objectArray.length ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(final double[] doubleArray){
		return new ReadOnlyList() {
			public Object get(int index) {
				return new Double( doubleArray[index] );
			}
			public int size() {
				return doubleArray.length ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(final java.util.Enumeration enum){
		final List list = new LinkedList();
		while( enum.hasMoreElements() ){
			list.add(enum.nextElement());
		}
		return new ReadOnlyList() {
			public Object get(int index) {
				return list.get(index);
			}
			public int size() {
				return list.size() ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(final float[] floatArray){
		return new ReadOnlyList() {
			public Object get(int index) {
				return new Float( floatArray[index] );
			}
			public int size() {
				return floatArray.length ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(final int[] intArray){
		return new ReadOnlyList() {
			public Object get(int index) {
				return new Integer( intArray[index] );
			}
			public int size() {
				return intArray.length ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(java.util.Iterator iterator){
		final List list = new LinkedList();
		while( iterator.hasNext() ){
			list.add(iterator.next());
		}
		return new ReadOnlyList() {
			public Object get(int index) {
				return list.get(index);
			}
			public int size() {
				return list.size() ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(final long[] longArray){
		return new ReadOnlyList() {
			public Object get(int index) {
				return new Long( longArray[index] );
			}
			public int size() {
				return longArray.length ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(java.util.Map m){
		return toForEachList(m.values());
	}
	    
	public static ReadOnlyList toForEachList(final Object[] objectArray){
		return new ReadOnlyList() {
			public Object get(int index) {
				return objectArray[index] ;
			}
			public int size() {
				return objectArray.length ;
			}
		}; 
	}
	public static ReadOnlyList toForEachList(final short[] shortArray){
		return new ReadOnlyList() {
			public Object get(int index) {
				return new Short( shortArray[index] );
			}
			public int size() {
				return shortArray.length ;
			}
		}; 
	}
	    
	public static ReadOnlyList toForEachList(String splitableString){
	    return toForEachList(splitableString,",");
	}
	public static ReadOnlyList toForEachList(String splitableString, String delims){
		final String[] a = splitableString.split(delims);
		return new ReadOnlyList() {
			public Object get(int index) {
				return a[index] ;
			}
			public int size() {
				return a.length ;
			}
		}; 
	}

	public static ReadOnlyList toForEachList(final java.lang.Object srcObject){
	    ReadOnlyList ret = null;
	    if( srcObject.getClass().isArray()){
	        ret = getArrayToReadOnlyList(srcObject);
	    }else{
	        ret = getObjectToReadOnlyList(srcObject);
	    }
	    if( ret == null ){
			ret = new ReadOnlyList() {
				public Object get(int index) {
					return index == 0 ? srcObject : null ;
				}
				public int size() {
					return 1 ;
				}
			}; 
	    }
	    return ret ;
	}
	private static ReadOnlyList getArrayToReadOnlyList(Object srcObject){
	    if( srcObject instanceof boolean[] ){
	        return toForEachList((boolean[])srcObject);
	    }
	    if( srcObject instanceof byte[] ){
	        return toForEachList((byte[])srcObject);
	    }
	    if( srcObject instanceof char[] ){
	        return toForEachList((char[])srcObject);
	    }
	    if( srcObject instanceof short[] ){
	        return toForEachList((short[])srcObject);
	    }
	    if( srcObject instanceof int[] ){
	        return toForEachList((int[])srcObject);
	    }
	    if( srcObject instanceof long[] ){
	        return toForEachList((long[])srcObject);
	    }
	    if( srcObject instanceof float[] ){
	        return toForEachList((float[])srcObject);
	    }
	    if( srcObject instanceof double[] ){
	        return toForEachList((double[])srcObject);
	    }
	    if( srcObject instanceof Object[] ){
	        return toForEachList((Object[])srcObject);
	    }
	    return null ;
	}
	private static ReadOnlyList getObjectToReadOnlyList(Object srcObject){
	    if( srcObject instanceof String ){
	        return toForEachList((String)srcObject);
	    }
	    if( srcObject instanceof Collection ){
	        return toForEachList((Collection)srcObject);
	    }
	    if( srcObject instanceof Map ){
	        return toForEachList((Map)srcObject);
	    }
	    if( srcObject instanceof Iterator ){
	        return toForEachList((Iterator)srcObject);
	    }
	    if( srcObject instanceof Enumeration ){
	        return toForEachList((Enumeration)srcObject);
	    }
	    return null ;
	}
}


