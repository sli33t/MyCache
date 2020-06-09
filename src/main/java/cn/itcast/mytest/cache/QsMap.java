package cn.itcast.mytest.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import org.springframework.cglib.beans.BeanMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义Map集合，以求实Qs开头
 * 
 * @author linbin
 */
public class QsMap extends ConcurrentHashMap<String, Object> implements Comparator<QsMap> {

	private static final long serialVersionUID = 1L;

	/**
	 * 构造方法
	 */
	public QsMap() {

	}
	
	/**
	 * 获取对应Key的Stirng值
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		Object obj = get(key);
		return obj == null ? "" : obj.toString();
	}

	/**
	 * 获取对应Key的Integer值
	 *
	 * @return
	 */
	public int getInt(String key) {
		Object o = get(key);
		if (o != null && !o.equals("")) {
			return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(key);
		} else {
			return 0;
		}
	}

	public Long getLong(String key){
		Object o = get(key);
		if (o != null && !o.equals("")) {
			return (Long) o;
		} else {
			return -1L;
		}
	}

	public double getDouble(String key) {
		Object o = get(key);
		if (o != null) {
			try {
				return o instanceof Number ? ((Number) o).doubleValue() : Double.parseDouble((String) o);
			} catch (Exception e) {

			}
		}
		return 0;
	}

	/**
	 * 获取对应Key的boolean值
	 * 
	 * @param key
	 * @return
	 */
	public boolean getBoolean(String key) {
		Object obj = get(key);
		return obj == null ? false : Boolean.valueOf(obj.toString());
	}
	
	/**
	 * 通过json转化为QsMap
	 * 
	 * @param objString
	 * @return
	 */
	public static QsMap fromObject(String objString) {
		if (StringUtil.isEmpty(objString)) {
			return new QsMap();
		} else {
			return JSONObject.parseObject(objString, QsMap.class);
		}
	}

	/**
	 * 通过key获取QsMap
	 * 
	 * @param key
	 * @return
	 */
	public QsMap getQsMap(String key) {
		Object o = get(key);
		if (StringUtil.isEmpty(key)) {
			return new QsMap();
		} else if (o == null) {
			return new QsMap();
		}
		String type = o.getClass().getName();
		if (this.getClass().getName().equals(type)) {
			return (QsMap) o;
		} else if (new String().getClass().getName().equals(type)) {
			return fromObject(o.toString());
		} else {
			return fromObject(o.toString());
		}
	}

	/**
	 * 通过key获取List
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<QsMap> getList(String key) {
		try {
			return (ArrayList<QsMap>) get(key);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<QsMap>();
		}
	}

	/**
	 * 将QsMap转化为String
	 */
	public String toString() {
		return JSON.toJSONString(this);
	}

	/**
	 * 克隆
	 */
	public QsMap clone() {
		QsMap map = new QsMap();
		for (Entry<String, Object> entry: this.entrySet()){
			map.put(entry.getKey(), entry.getValue(), false);
		}
		return map;
	}

	@Override
	public int compare(QsMap o1, QsMap o2) {
		return new Integer(o1.getClass().getName()).compareTo(new Integer(o2.getClass().getName()));
	}
	
	/**
	 * 检查值是否存在
	 */
	public boolean contains(Object value){
		return super.containsValue(value);
	}
	
	/**
	 * 检查键是否存在
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key){
		return super.containsKey(key);
	}
	
	/**
	 * 将Map里的key下划线转小驼峰
	 * @return
	 */
	public QsMap underlineToCamel(){
		QsMap map = new QsMap();
		for (Entry<String, Object> entry: this.entrySet()){
			map.put(underlineToCamel(entry.getKey()), entry.getValue());
		}
		return map;
	}
	
	/**
	 * 将Map里的key小驼峰转下划线
	 * @param isUpper：true：统一都转大写；false-统一转小写
	 * @return
	 */
	public QsMap camelToUnderline(boolean isUpper){
		QsMap map = new QsMap();
		for (Entry<String, Object> entry: this.entrySet()){
			map.put(camelToUnderline(entry.getKey(), isUpper), entry.getValue());
		}
		return map;
	}
	
	/**
	 * 获取map里所有的key(重点注意所有的key都加了单引号)
	 * @param link 连接符，一般是逗号
	 * @return
	 */
	public String getKeys(String link){
		String keys = "";
		for (Entry<String, Object> entry: this.entrySet()){
			keys = keys + "'" + entry.getKey() + "'" + link; 
		}
		
		//去掉最后一个逗号
		keys = keys.substring(0, keys.length()-1);
		return keys;
	}
	
	/**
	 * 重写put方法，默认转化小驼峰命名
	 * 
	 * @param key
	 * @param value
	 */
	public Object put(String key, Object value){
		return this.put(key, value, true);
	}
	
	/**
	 * 重写put方法，转化小驼峰命名
	 * @param key
	 * @param value
	 * @param toCamel：true:将key转化为小驼峰，false:不转化
	 * @return
	 */
	public Object put(String key, Object value, boolean toCamel){
		String newKey;
		if (toCamel){
			newKey = underlineToCamel(key);	
		}else {
			newKey = key;
		}
		
		if (value==null){
			if (value instanceof Number||value instanceof Integer||value instanceof Float||value instanceof Double||value instanceof Short){
				//value.getClass().getName().equals(Long.class.getName())
				value = 0;
			}else {
				value = "";
			}
		}
		
		return super.put(newKey, value);
	}
	
	/**
	 * 下划线转小驼峰
	 * @param param 
	 * @return
	 */
	public static String underlineToCamel(String param) {
		char UNDERLINE = '_';
		
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        
        int len = param.length();

        //不包含下划线的，返回原值
        if (!param.contains("_")){
        	boolean isUpper = true;
        	for (int i = 0; i < len; i++) {
        		char c = param.charAt(i);
        		if (Character.isLowerCase(c)){
        			isUpper = false; //检查到小写
        			break;
        		}
        	}
        	
        	//全部是大写的
        	if (isUpper){
        		return param.toLowerCase();        		
        	}else {
        		return param;				
			}
        }
        
        StringBuilder sb = new StringBuilder(len);
        Boolean flag = false; // "_" 后转大写标志,默认字符前面没有"_"
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE) {
                flag = true;
                continue;   //标志设置为true,跳过
            } else {
                if (flag == true) {
                    //表示当前字符前面是"_" ,当前字符转大写
                    sb.append(Character.toUpperCase(param.charAt(i)));
                    flag = false;  //重置标识
                } else {
                    sb.append(Character.toLowerCase(param.charAt(i)));
                }
            }
        }
        return sb.toString();
    }
	
	/**
	 * 小驼峰转下划线
	 * @param param
	 * @param toUpper： true：统一都转大写；false-统一转小写
	 * @return
	 */
	public static String camelToUnderline(String param, boolean toUpper) {
		char UNDERLINE = '_';
		
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        
        int len = param.length();

        //已经包含下划线的，返回原值
        if (param.contains("_")){
        	return param;
        }
        
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(UNDERLINE);
            }
            if (toUpper) {
                sb.append(Character.toUpperCase(c));  //统一都转大写
            } else {
                sb.append(Character.toLowerCase(c));  //统一都转小写
            }
        }
        return sb.toString();
    }
	
	/**
	 * 将bean转化为map
	 * @param bean
	 * @return
	 */
	public static <T> QsMap beanToMap(T bean){
		QsMap qsMap = new QsMap();
		if (bean!=null){
			BeanMap beanMap = BeanMap.create(bean);
			for (Object key : beanMap.keySet()) {
				Object o = beanMap.get(key);
				//ConcurrentHashMap 不能put null 的原因是因为：无法分辨是key没找到的null还是有key值为null值，
				//页这在多线程里面是模糊不清的，所以压根就不让put null。
				if (key!=null){
					if (o==null){
						o = "";
					}
					qsMap.put(key+"", o);					
				}
            }
		}
		return qsMap;
	}
	
	private static void beanMapPut(QsMap qsMap, BeanMap beanMap, List<String> keys, String key){
		if (!keys.contains(key)){
			return;
		}
		
		Object object = qsMap.get(key);
		if (object instanceof Integer || object instanceof Date || object instanceof Double){
			beanMap.put(key, qsMap.getInt(key));
		}else {
			beanMap.put(key, qsMap.getString(key));
		}
	}
	
	/**
	 * 
	 * @param map
	 * @param bean
	 * @return
	 */
	public static <T> T mapToBean(QsMap map, T bean){
		//TODO o instanceof Number ? ((Number) o).intValue() : (int) getDouble(key)
		BeanMap beanMap = BeanMap.create(bean);
		//beanMap.putAll(map);
		
		List<String> keys = new ArrayList<>();
		for (Object set: beanMap.entrySet()){
			String newKey = set.toString().substring(0, set.toString().indexOf("="));
			keys.add(newKey);
		};
		
		String key;
        for(Iterator<String> it = map.keySet().iterator(); it.hasNext(); beanMapPut(map, beanMap, keys, key))
            key = (String) it.next();
        
		return bean;
	}
	

	public int getPageIndex(){
		Object o = get("pageIndex");
		if (o==null||o.equals("")){
			return 1; //默认第一页
		}else {
			return this.getInt("pageIndex");			
		}
	}
	
	public int getPageSize(){
		Object o = get("pageSize");
		if (o==null||o.equals("")){
			return 10; //默认10行
		}else {
			return this.getInt("pageSize");			
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		
	}
}
