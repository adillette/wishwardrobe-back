package today.wishwordrobe.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"Today.WishWordrobe.clothes.infrastructure", "Today.WishWordrobe.user.infrastructure"},
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
)
public class DataSourceConfig {
/*
    @Primary
    @Bean(name="mainDataSource")
    @ConfigurationProperties(prefix = "mainTransactionManager")
    public DataSource mainDataSource(){
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName("oracle.jdbc.OracleDriver")
                .url("jdbc:oracle:thin:@localhost:1521:orcl")
                .username("wish")
                .password("a123")
                .build();
    }
 */
    @Primary
    @Bean(name="mainDataSource")
  //  @ConfigurationProperties(prefix = "mainTransactionManager")
    public DataSource mainDataSource(){
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:wishwordrobe")
                .username("sa")
                .password("")
                .build();
    }

    @Primary
    @Bean(name = "mainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactoryBean() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(mainDataSource());


        String[] domainPackages ={
                "Today.WishWordrobe.user.domain",
                "Today.WishWordrobe.weather.domain",
                "Today.WishWordrobe.clothes.domain",
                "Today.WishWordrobe.domain"
        };
        em.setPackagesToScan(domainPackages);
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        /*
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.Oracle19cDialect");
        jpaProperties.put("hibernate.show_sql", "true");
        jpaProperties.put("hibernate.format_sql", "true");
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");
        em.setJpaProperties(jpaProperties);

        return em;
         */


        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        jpaProperties.put("hibernate.show_sql", "true");
        jpaProperties.put("hibernate.format_sql", "true");
        jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");
        em.setJpaProperties(jpaProperties);

        return em;
    }

    @Primary
    @Bean(name="mainTransactionManager")
    public JpaTransactionManager mainTransaction(){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(mainEntityManagerFactoryBean().getObject());
        return transactionManager;

    }
}
