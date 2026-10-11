package com.ecommerce.order.support;

import javax.sql.DataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/** Membungkus DataSource aplikasi agar setiap SQL tercatat. */
@TestConfiguration(proxyBeanMethods = false)
public class SqlRecorderConfig {

    @Bean
    SqlRecorder sqlRecorder() {
        return new SqlRecorder();
    }

    @Bean
    static BeanPostProcessor recordingDataSource(
            ObjectProvider<SqlRecorder> recorder) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean,
                    String name) {
                if (bean instanceof DataSource ds) {
                    return ProxyDataSourceBuilder.create(ds)
                            .name("recorded")
                            .listener(recorder.getObject())
                            .build();
                }
                return bean;
            }
        };
    }
}
