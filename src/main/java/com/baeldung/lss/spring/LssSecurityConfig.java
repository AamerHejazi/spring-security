package com.baeldung.lss.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@EnableWebSecurity
@EnableAsync
//This is to allow @PreAuthorize and @secured annotations
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class LssSecurityConfig {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    // I add @Lazy PasswordEncoder to remove the cycle of dependencies
    @Autowired
    public LssSecurityConfig(UserDetailsService userDetailsService, @Lazy PasswordEncoder passwordEncoder, DataSource dataSource) {
        super();
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.dataSource = dataSource;
    }
    //second way to solve problem of thread auth issue delegating security context
    @PostConstruct
    public void enableAuthCtxOnSpawnedThreads() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
    //     first way to solve problem of thread auth issue  delegating security context
//    @Bean
//    public MethodInvokingFactoryBean methodInvokingFactoryBean() {
//        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
//        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
//        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
//        methodInvokingFactoryBean.setArguments(new String[]{SecurityContextHolder.MODE_INHERITABLETHREADLOCAL});
//        return methodInvokingFactoryBean;
//    }
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception { // @formatter:off 
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        /*old implementation*/
        /*auth.
            inMemoryAuthentication().passwordEncoder(passwordEncoder)
                .withUser("user").password(passwordEncoder.encode("pass"))
                .roles("USER")
                .and()
                .withUser("admin").password(passwordEncoder.encode("admin"))
                .roles("ADMIN");*/
    } // @formatter:on

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {// @formatter:off
        http
                .authorizeRequests()
                .antMatchers("/user/delete/**").hasAuthority("ADMIN")
                .antMatchers("/user/modify/**").hasAnyAuthority("USER", "ADMIN")
                .antMatchers("/user/*form").access("hasAnyAuthority('ADMIN')")
                // another way to use hasAnyAuthority but with access Expressions
                .antMatchers("/secured").access("hasAnyAuthority('ADMIN')")
                //.antMatchers("/secured").access("request.method == 'GET'")
                // allow post request with username = user
                //.antMatchers("/secured").access("request.method != 'POST' and principal.username == 'user'")
                //not allow IP address
                //.antMatchers("/secured").not().access("hasIpAddress('129.10.5.2/24') and hasIpAddress('129.10.5.1/24')")
                //allow IP address local host also this will override previos one
                //.antMatchers("/secured").access("hasIpAddress('::1')")
                //use not to block below IP address
                //.antMatchers("/secured").not().access("hasIpAddress('129.10.5.1/24')")
                .antMatchers("/signup", "/user/register", "/registrationConfirm*", "/forgotPassword*",
                        "/user/resetPassword*",
                        "/user/changePassword*",
                        "/user/savePassword*")

                .permitAll()
                .anyRequest()
                .authenticated()

                .and()
                .formLogin()
                .loginPage("/login").permitAll()
                .loginProcessingUrl("/doLogin")
// deprecated
                /* .and()
                 .rememberMe()
                         .tokenValiditySeconds(604800)
                         .key("1ssAppkey")
                         //.useSecureCookie(true)
                         .rememberMeCookieName("sticky")
                         .rememberMeParameter("remember")*/
                .and()
                .rememberMe()
                .tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(604800)
                .rememberMeCookieName("sticky")
                .rememberMeParameter("remember")

                .and()
                .logout()
                .permitAll()
                .logoutUrl("/logout")

                .and()
                    .csrf().disable();

        return http.build();
    }


    //This is only to manage the cookies also we should create the table manually
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        final JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
//    @Bean
//    public PasswordEncoder encoder() {
//        return new Md5PasswordEncoder();
//    }
}
