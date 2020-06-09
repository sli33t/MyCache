package cn.itcast.mytest.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QsCacheConfig {

    @Bean
    public QsCache getQsCache(){
        return new QsCache();
    }
}
