package com.pistis.board.global.config.security;

import com.pistis.board.global.config.security.exception.CustomAccessDeniedHandler;
import com.pistis.board.global.config.security.exception.CustomAuthenticationEntryPoint;
import com.pistis.board.global.config.security.exception.LoginFailureHandler;
import com.pistis.board.global.config.security.exception.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    // UserDetailsService 설정
    @Bean
    public UserDetailsService userDetailsService() {
//        return new BoardUserDetailsService(); // 실 사용 UserDetailsService
        return inMemoryUserDetailsService();
    }

    // PasswordEncoder 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // 강도 증가할 수록 지연시간 오래걸림, 권장 지연 1s
    }
//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        Map encoders = new HashMap();
//        encoders.put("bcrypt", new BCryptPasswordEncoder(10));
//        return new DelegatingPasswordEncoder("bcrypt", encoders);
//    }

    // DaoAuth 설정
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        // UserDetailsService 와 PasswordEncoder 를 @Bean 등록 해두면
        // Spring이 알아서 AuthenticationManagerBuilder 를 설정해준다. 는데 이거 빈등록 안하면 인식을 못한다.... 뭘 해야하나
        // UserDetailsService 만드는 거 아직 잘 모르겠으니깐 일단 inMemory 용으로 만들자...
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setPasswordEncoder(passwordEncoder());                 // passwordEncoder 설정
        auth.setUserDetailsService(userDetailsService());           // userDetailsService 추가
        return auth;
    }

    // inMemoryUser 생성
    @Bean
    public UserDetailsService inMemoryUserDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("1234"))
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("1234"))
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }

    // HttpSecurity 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .exceptionHandling()    // 예외 설정
//                .authenticationEntryPoint(authenticationEntryPoint)     // 인증되지 않은 사용자 처리 ( 로그인 상태가 아님 )
                .accessDeniedHandler(accessDeniedHandler);              // 사용자 권한 없음 에러 처리 ( 관리자가 아님 || 멤버가 아님 )
        http
                .authorizeRequests()    // 권한 설정
                .antMatchers("/admin/**").hasRole("ADMIN")  // 해당 경로는 ADMIN 권한만 접근 가능
                .antMatchers("/user/**").hasRole("USER")    // 해당 경로는 USER 권한만 접근 가능
                .anyRequest().permitAll();                             // 나머지 경로는 모두다 접근 가능
        http
                .anonymous()            // 비인가 유저 principal 처리
                .principal("미인증유저");
        http
                .formLogin();            // 로그인 설정
//                .loginPage("/login")
//                .loginProcessingUrl("/login")
//                .successHandler(loginSuccessHandler)
//                .failureHandler(loginFailureHandler);
//
//         .loginPage("/login-page")
//                .loginProcessingUrl("/login-process")
//                .defaultSuccessUrl("/main")
//                .successHandler(new CustomAuthenticationSuccessHandler("/main"))
//                .failureUrl("login-fail")
//                .failureHandler(new CustomAuthenticationFailureHandler("/login-fail"))



        http
                .rememberMe()  // 로그인 기억하기 - 기본설정 2주
                .userDetailsService(userDetailsService())
                .rememberMeParameter("rememberMe")
                .key("로그인 기억하기");
        http
                .logout()               // 로그아웃 설정
                .logoutSuccessUrl("/");
        return http.build();
    }

    // WebSecurity 설정 ( HttpSecurity 보다 우선순위 )
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // antMatchers에 파라미터로 넘겨주는 endpoints는 Spring Security Filter Chain을 거치지 않기 때문에
        // '인증' , '인가' 서비스가 모두 적용되지 않는다 (필터 자체를 통과하지 않는다)
        // 완전 무시한다. isAnonymous 권한이 아니라 그냥 권한이 아예 안 읽힌다.
        // 떄문에 만약 모두에게 열린 권한이라면 모든 필터를 통과하고 요청을 허용하는 permitAll 보단 ignoring 이 성능면에서 이득이다.

        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations()); // 정적리소스(static 폴더) 보안 무시
    }
}