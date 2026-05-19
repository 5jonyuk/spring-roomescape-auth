package roomescape.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.common.interceptor.AdminInterceptor;
import roomescape.common.interceptor.AuthenticationInterceptor;
import roomescape.common.argumentresolver.LoginMemberArgumentResolver;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final AuthenticationInterceptor authenticationInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/logout", "/themes/popular");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns(
                        "/times/**",
                        "/themes/**",
                        "/schedules/**"
                )
                .excludePathPatterns(
                        "/themes",
                        "/themes/popular",
                        "/times/availability"
                );
    }
}
