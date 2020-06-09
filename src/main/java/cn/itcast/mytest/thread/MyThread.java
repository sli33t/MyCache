package cn.itcast.mytest.thread;

import cn.itcast.mytest.cache.QsCache;

public class MyThread implements Runnable {

    public void run() {

        while (true){
            QsCache.putCache("name", "tom");

            Object name = QsCache.getCache("name");
            System.out.println("取出缓存数据: "+name);

            try {
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
