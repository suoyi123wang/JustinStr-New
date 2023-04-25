package cn.ios.casegen.variable;

import cn.ios.casegen.util.BaseUtil;
import cn.ios.casegen.util.TypeUtil;
import cn.ios.casegen.util.ClassUtil;
import soot.SootClass;
import soot.Type;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class FullClassType {
	private Type type = null;
	private List<FullClassType> genericElements = new ArrayList<>();

	public FullClassType(Type type, String genericInfo) {
		this.type = type;
		if (genericInfo != null && !genericInfo.isEmpty()) {
			addGenericInfo(genericInfo);
		}
	}

	public void addGenericInfo(String genericInfo){
		if (TypeUtil.isMapType(type)) {
			int i = splitSemicolon(genericInfo);

			String keyStr = genericInfo.substring(0, i);
			String valueStr = genericInfo.substring(i + 1);
			keyStr = keyStr.substring(keyStr.indexOf("L") + 1);
			valueStr = valueStr.substring(valueStr.indexOf("L") + 1);

			String keyGenericInfo = ClassUtil.spliceGenericInfo(keyStr);
			String valueGenericInfo = ClassUtil.spliceGenericInfo(valueStr);

			String keyClassName = keyGenericInfo.isEmpty()? keyStr: BaseUtil.substringToStr(keyStr, keyGenericInfo);
			String valueClassName = valueGenericInfo.isEmpty()? valueStr: BaseUtil.substringToStr(valueStr, valueGenericInfo);
			if (keyClassName.contains("<"))  keyClassName = BaseUtil.substringToStr(keyClassName, "<");
			if (valueClassName.contains("<"))  valueClassName = BaseUtil.substringToStr(valueClassName, "<");

			SootClass keyClass = ClassUtil.getSootClassByName(keyClassName);
			SootClass valueClass = ClassUtil.getSootClassByName(valueClassName);
			if (keyClass == null || valueClass == null) {
				throw new IllegalArgumentException("Exception in FullClassType.addGenericInfo :  Map GenericInfo Error");
			}

			genericElements.add(new FullClassType(keyClass.getType(), keyGenericInfo));
			genericElements.add(new FullClassType(valueClass.getType(), valueGenericInfo));

		} else if (TypeUtil.isCollectionType(type)){
			// List/set
			genericInfo = genericInfo.substring(genericInfo.indexOf("L") + 1);
			String smallGeneric = ClassUtil.spliceGenericInfo(genericInfo);
			String className = smallGeneric.isEmpty()? genericInfo: BaseUtil.substringToStr(genericInfo, smallGeneric);
			if (className.contains("<"))  className = BaseUtil.substringToStr(className, "<");

			SootClass collectionClass = ClassUtil.getSootClassByName(className);
			if (collectionClass == null ) {
				throw new IllegalArgumentException("Exception in FullClassType.addGenericInfo :  Collection GenericInfo Error");
			}
			genericElements.add(new FullClassType(collectionClass.getType(), smallGeneric));
		}
	}

	public Type getType() {
		return type;
	}

	public List<FullClassType> getGenericElements() {
		return genericElements;
	}

	private int splitSemicolon(String str){
		Deque<Integer> deque = new LinkedList<>();
		for (int i = 0; i < str.length(); ++i) {
			if (str.charAt(i) == '<') {
				deque.push(i);
			} else if (str.charAt(i) == '>') {
				if (!deque.isEmpty()) {
					deque.pop();
				} else {
					throw new IllegalArgumentException("Exception in FullClassType");
				}
			} else if (str.charAt(i) == ';') {
				if (deque.isEmpty()) {
					return i;
				}
			}
		}
		return -1;
	}
}
