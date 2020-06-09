package cn.itcast.mytest.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * 自定义缓存，以求实Qs开头
 *
 * @author linbin
 */
public class QsCache {

    //日志，不解释
    private static Logger LOGGER = LoggerFactory.getLogger(QsCache.class);

    //记录最大的缓存数量，超出1000再添加的话会自动清除一个
    private static final Integer MAX_NUMBER = 1000;

    //当前缓存数量
    private static Integer THIS_SIZE = 0;

    //设置一个自动清除的时间
    public static final Long ONE_MINUTE = 60 * 1000L;

    //设置缓存对象，类似于redis的16个数据库
    private static final QsMap CACHE_MAP = new QsMap();
    private static final QsMap CACHE_MAP_1 = new QsMap();
    private static final QsMap CACHE_MAP_2 = new QsMap();
    private static final QsMap CACHE_MAP_3 = new QsMap();
    private static final QsMap CACHE_MAP_4 = new QsMap();
    private static final QsMap CACHE_MAP_5 = new QsMap();
    private static final QsMap CACHE_MAP_6 = new QsMap();
    private static final QsMap CACHE_MAP_7 = new QsMap();
    private static final QsMap CACHE_MAP_8 = new QsMap();
    private static final QsMap CACHE_MAP_9 = new QsMap();
    private static final QsMap CACHE_MAP_10 = new QsMap();
    private static final QsMap CACHE_MAP_11 = new QsMap();
    private static final QsMap CACHE_MAP_12 = new QsMap();
    private static final QsMap CACHE_MAP_13 = new QsMap();
    private static final QsMap CACHE_MAP_14 = new QsMap();
    private static final QsMap CACHE_MAP_15 = new QsMap();

    //增加threadLocal，让线程互不影响
    private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

    private static final String DISK_FILE = "D://test.lb";

    //来个有序的列表，用于存储每个缓存内容，就是将 CACHE_MAP 按顺序存入
    private static final List<String> CACHE_LIST = new LinkedList<>();

    //清理线程是否启动
    public static boolean CLEAN_THREAD_IS_RUN = false;
    //持久化线程是否启动
    public static boolean AOF_THREAD_IS_RUN = false;

    private static final String CACHE_KEY = "key";
    private static final String CACHE_VALUE = "value";
    private static final String CACHE_TIME = "time";

    //持久化默认关闭
    public static boolean AOF_OPEN = false;

    private static final String FILE_LIST = "fileList";


    /**
     * 检查一下缓存数量
     */
    private static void checkSize() {
        if (THIS_SIZE >= MAX_NUMBER) {
            deleteTimeOut(); //先删除过期的
        }

        if (THIS_SIZE >= MAX_NUMBER) {
            deleteLast(); //如果还是大于最大数量，再删除过期的
        }

        LOGGER.info("check cache successful!");
    }

    /**
     * 默认不清除
     * @param key
     * @param value
     */
    public static void putCache(String key, Object value){
        putCache(key, value, -1L);
    }

    /**
     * 默认写入第一个缓存
     * @param key
     * @param value
     * @param time
     */
    public static void putCache(String key, Object value, Long time){
        putCache(key, value, time, 0);
    }

    /**
     * 写入缓存
     * @param key
     * @param value
     * @param time
     */
    public static void putCache(String key, Object value, Long time, Integer index){
        Long ttlTime = null;
        if (time <= 0L) {
            if (time == -1L) {
                ttlTime = -1L;  //当time<=0时，给ttlTime赋值-1，不走线程
            } else {
                return;
            }
        }

        if (ttlTime == null) {
            ttlTime = System.currentTimeMillis() + time; //当前毫秒+时长
        }

        //检查缓存数量是否超出最大数量
        checkSize();
        saveLiveCache(key);

        //持久化
        writeFileThread();

        THIS_SIZE = THIS_SIZE + 1;
        QsMap qsCacheMap = new QsMap();
        qsCacheMap.put(CACHE_KEY, key);
        qsCacheMap.put(CACHE_VALUE, value);
        qsCacheMap.put(CACHE_TIME, ttlTime);

        THREAD_LOCAL.set(index);

        if (index==0){
            CACHE_MAP.put(key, qsCacheMap);
        }else if (index==1){
            CACHE_MAP_1.put(key, value);
        }else if (index==2){
            CACHE_MAP_2.put(key, value);
        }else if (index==3){
            CACHE_MAP_3.put(key, value);
        }else if (index==4){
            CACHE_MAP_4.put(key, value);
        }else if (index==5){
            CACHE_MAP_5.put(key, value);
        }else if (index==6){
            CACHE_MAP_6.put(key, value);
        }else if (index==7){
            CACHE_MAP_7.put(key, value);
        }

        //8,9,10 ...

        LOGGER.info("put cache [key: "+key+"; index: "+index+"] successful!");
    }

    /**
     * 根据key检查缓存，如果超时则删除
     * @param cacheKey
     * @return
     */
    private static boolean checkCache(String cacheKey) {
        QsMap qsCacheMap = CACHE_MAP.getQsMap(cacheKey);

        if (qsCacheMap==null||qsCacheMap.size()==0){
            return false;
        }

        Long time = qsCacheMap.getLong(CACHE_TIME);
        if (time == -1L) {
            return true;
        }

        LOGGER.info("[key: "+cacheKey+" time: "+time+"] sysTime: "+System.currentTimeMillis());

        //判断超时
        if (time < System.currentTimeMillis()) {
            deleteCache(cacheKey);
            return false;
        }

        LOGGER.info("check cache successful!");
        return true;
    }


    /**
     * 默认读取第一个缓存
     * @param key
     * @return
     */
    public static Object getCache(String key){
        return getCache(key, 0);
    }

    /**
     * 通过key从缓存中获取
     * @param key
     * @return
     */
    public static Object getCache(String key, Integer index){
        //开启清除线程，先清除一下
        startCleanThread();

        //持久化
        writeFileThread();

        //如果存在这个key，就走缓存，如果不存在，就取本地文件
        if (checkCache(key)){
            //先记录活跃
            saveLiveCache(key);

            THREAD_LOCAL.get();

            QsMap qsCacheMap = new QsMap();
            if (index==0){
                qsCacheMap = QsMap.fromObject(CACHE_MAP.get(key).toString());
            }else if (index==1){
                qsCacheMap = QsMap.fromObject(CACHE_MAP_1.get(key).toString());
            }else if (index==2){
                qsCacheMap = QsMap.fromObject(CACHE_MAP_2.get(key).toString());
            }else if (index==3){
                qsCacheMap = QsMap.fromObject(CACHE_MAP_3.get(key).toString());
            }else if (index==4){
                qsCacheMap = QsMap.fromObject(CACHE_MAP_4.get(key).toString());
            }else if (index==5){
                qsCacheMap = QsMap.fromObject(CACHE_MAP_5.get(key).toString());
            }else if (index==6){
                qsCacheMap = QsMap.fromObject(CACHE_MAP_6.get(key).toString());
            }else if (index==7){
                qsCacheMap = QsMap.fromObject(CACHE_MAP_7.get(key).toString());
            }

            //8,9,10...

            LOGGER.info("get cache [key: "+key+"; index: "+index+"] successful!");

            if (qsCacheMap!=null){
                return qsCacheMap.get(CACHE_VALUE);
            }
        }else {
            if (AOF_OPEN){
                Object o = readFile();
                LOGGER.info(o.toString());

                if (o==null||o.equals("")){
                    writeFile();
                }

                //写完file还是null，就证明缓存也已经没有了
                if (o==null||o.equals("")){
                    return null;
                }

                List<QsMap> list = JSONObject.parseArray(o.toString(), QsMap.class);
                QsMap qsCacheMap = new QsMap();

                for (QsMap map : list) {
                    if (map.containsKey(key)){
                        qsCacheMap = QsMap.fromObject(map.get(key).toString());
                        break;
                    }
                }

                LOGGER.info("get cache [key: "+key+"; index: "+index+"] successful!");

                if (qsCacheMap!=null){
                    return qsCacheMap.get(CACHE_VALUE);
                }
            }
        }

        return null;
    }

    /**
     * 获取所有现在的key
     * @return
     */
    public static List<String> getKeys(){
        return CACHE_LIST;
    }

    /**
     * 开启清理过期缓存的线程
     */
    private static void startCleanThread() {
        if (!CLEAN_THREAD_IS_RUN) {
            DeleteTimeOutThread deleteTimeOutThread = new DeleteTimeOutThread();
            Thread thread = new Thread(deleteTimeOutThread);
            //设置为后台守护线程
            thread.setDaemon(true);
            thread.start();
            LOGGER.info("clean thread start successful!");
        }
    }


    /**
     * 使用记录，主要是把活跃的放在最上面
     * @param cacheKey
     */
    private static synchronized void saveLiveCache(String cacheKey) {
        synchronized (CACHE_LIST) {
            CACHE_LIST.remove(cacheKey);
            CACHE_LIST.add(0, cacheKey);
            LOGGER.info("live cache saved!");
        }
    }

    /**
     * 清理线程开启
     */
    static void setCleanThreadRun() {
        CLEAN_THREAD_IS_RUN = true;
        LOGGER.info("clean thread open successful!");
    }

    /**
     * 持久化线程开启
     */
    static void setAofThreadIsRun(){
        AOF_THREAD_IS_RUN = true;
        LOGGER.info("aof thread open successful!");
    }

    /**
     * 删除超时的缓存
     */
    public static void deleteTimeOut() {
        List<String> deleteKeyList = new LinkedList<>();

        for (Map.Entry<String, Object> entry : CACHE_MAP.entrySet()) {
            String key = entry.getKey();

            QsMap qsCacheMap = QsMap.fromObject(entry.getValue().toString());
            Long time = qsCacheMap.getLong(CACHE_TIME);
            //设置时间小于当前时间 并且 设置时间不为1，此时就删之
            if (time < System.currentTimeMillis() && time != -1L) {
                deleteKeyList.add(key);
            }
        }

        for (String deleteKey : deleteKeyList) {
            deleteCache(deleteKey);
        }

        LOGGER.info("deleteCache's count :" + deleteKeyList.size());
    }

    /**
     * 删除最后10个不用的
     */
    private static void deleteLast() {
        String cacheKey = null;
        synchronized (CACHE_LIST) {
            if (CACHE_LIST.size() >= THIS_SIZE - 10) {
                cacheKey = CACHE_LIST.remove(CACHE_LIST.size() - 1);
            }
        }
        if (cacheKey != null) {
            deleteCache(cacheKey);
            LOGGER.info("delete last 10 cache!");
        }
    }

    /**
     * 根据键删除缓存
     * @param cacheKey
     */
    public static void deleteCache(String cacheKey) {
        Object cacheValue = CACHE_MAP.remove(cacheKey);
        CACHE_LIST.remove(cacheKey);
        if (cacheValue != null) {
            THIS_SIZE = THIS_SIZE - 1;
            LOGGER.info("this size - 1 :" + cacheKey);
        }
    }

    /**
     * 清除所有的key
     */
    public static void clear() {
        CACHE_MAP.clear();
        THIS_SIZE = 0;
        LOGGER.info("clear all keys !");
    }


    /**
     * 读取文件
     * @return
     */
    public static Object readFile(){
        File file = new File(DISK_FILE);
        if(file.isFile() && file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuffer sb = new StringBuffer();
                String text = null;
                while((text = bufferedReader.readLine()) != null){
                    sb.append(text);
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 写入文件
     */
    public static void writeFile(){
        /*FileOutputStream fileOutputStream = null;
        File file = new File(DISK_FILE);
        try {
            if(!file.exists()){
                //判断文件是否存在，如果不存在就新建一个txt
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            List<QsMap> list = new ArrayList<QsMap>();

            *//*Object o = readFile();
            if (o!=null&&!o.equals("")){
                list = JSONObject.parseArray(o.toString(), QsMap.class);
            }*//*

            if (CACHE_MAP!=null&&CACHE_MAP.size()>0){

                if (!list.contains(CACHE_MAP)){
                    list.add(CACHE_MAP);
                }

                *//*QsMap map = new QsMap();
                map.put(FILE_LIST, list);*//*
                String writeStr = JSON.toJSONString(list);
                fileOutputStream.write(writeStr.getBytes());
            }

            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/


        try {//流的套接
            FileOutputStream fout = new FileOutputStream(DISK_FILE);
            DataOutputStream dout = new DataOutputStream(fout);

            String writeStr = "";
            List<QsMap> list = new ArrayList<QsMap>();
            if (CACHE_MAP!=null&&CACHE_MAP.size()>0){
                if (!list.contains(CACHE_MAP)){
                    list.add(CACHE_MAP);
                }
                writeStr = JSON.toJSONString(list);

                Scanner scanner = new Scanner(writeStr);
                String line = scanner.nextLine();
                System.out.println("input:"+line);
                try {
                    File writeName = new File(DISK_FILE); // 相对路径，如果没有则要建立一个新的output.txt文件

                    if (!writeName.exists()){
                        writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
                    }

                    try (FileWriter writer = new FileWriter(writeName);
                         BufferedWriter out = new BufferedWriter(writer)
                    ) {
                        out.write(line);
                        out.flush(); // 把缓存区内容压入文件
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            fout.close();
            dout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 写入文件线程
      */
    private static void writeFileThread(){
        //如果没有开启才启
        if (AOF_OPEN&&!AOF_THREAD_IS_RUN){
            WriteFileThread writeFileThread = new WriteFileThread();
            Thread thread = new Thread(writeFileThread);
            thread.setDaemon(true);
            thread.start();
            LOGGER.info("aof thread open successful!");
        }
    }


    //RDB(Redis DataBase) & AOF(Append Only File)
    public static void aof(Integer open){
        if (open==1){
            AOF_OPEN = true;
            LOGGER.info("aof open success!");
        }else if (open==0){
            AOF_OPEN = false;
            LOGGER.info("aof close success!");
        }
    }

}

/**
 * 清除超时缓存的线程
 */
class DeleteTimeOutThread implements Runnable{

    @Override
    public void run() {
        QsCache.setCleanThreadRun();
        while (true) {
            QsCache.deleteTimeOut();
            try {
                Thread.sleep(QsCache.ONE_MINUTE);
                System.out.println("clean thread run successful!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


/**
 * 持久化
 */
class WriteFileThread implements Runnable{

    @Override
    public void run() {
        QsCache.setAofThreadIsRun();
        //开启持久化
        if (QsCache.AOF_OPEN){
            while (true){
                QsCache.writeFile();
                System.out.println("cache aof successful!");
                try {
                    Thread.sleep(QsCache.ONE_MINUTE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
