package app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * 允许的跨域来源。
     * DICE_ROOM_SITE 配置（与 Caddyfile、run.sh 共用同一变量）。
     * 可以是 example.com，也可以是 http://localhost:8088 这样的完整 origin。
     */
    private String[] allowedOrigins() {
        List<String> origins = new ArrayList<>();
        origins.add("http://localhost:*");
        origins.add("http://127.0.0.1:*");

        String site = System.getenv("DICE_ROOM_SITE");
        if (site != null) {
            site = site.trim();
            if (!site.isEmpty() && !site.equals("localhost")) {
                if (site.startsWith("http://") || site.startsWith("https://")) {
                    origins.add(site);
                } else {
                    origins.add("https://" + site);
                    origins.add("http://" + site);
                }
            }
        }
        return origins.toArray(new String[0]);
    }
}
