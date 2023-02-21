package com.baeldung.lss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class PersistenceConfig {

    //old
    /*@Bean
    public DataSource dataSource(){
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).setName("learn-spring-db").build();
    }*/

    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        // also dependency inside pom is commented
//        dataSource.setDriverClassName("org.h2.Driver");
//        dataSource.setUrl("jdbc:h2:mem:learn-spring-db;DB_CLOSE_DELAY=-1");
//        dataSource.setUsername("username");
//        dataSource.setPassword("password");
//        return dataSource;
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/securitycourse");
        dataSource.setUsername("TestJPA");
        dataSource.setPassword("12345678");
        return dataSource;
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.baeldung.lss");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        //# Hibernate
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("defer-datasource-initialization", "true");
        //# Persistence
        properties.setProperty("javax.persistence.validation.mode","none");
        //properties.setProperty("open-in-view","false"); not working
        //properties.setProperty("database-platform","org.hibernate.dialect.MySQLDialect");
        em.setJpaProperties(properties);

        return em;
    }

}
