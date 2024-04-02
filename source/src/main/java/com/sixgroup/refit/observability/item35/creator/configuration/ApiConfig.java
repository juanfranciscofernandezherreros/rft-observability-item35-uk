package com.sixgroup.refit.observability.item35.creator.configuration;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.CookieManager;
import java.net.CookiePolicy;

@Configuration
@AllArgsConstructor
@Slf4j
public class ApiConfig {

    private final ApiClouderaProperties apiClouderaProperties;

    @Bean
    public OkHttpClient okHttpClient() {

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        OkHttpClient.Builder builder = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(cookieManager));
        builder.addInterceptor(chain -> {
            Request originalRequest = chain.request();
            Request.Builder requestBuilder = originalRequest.newBuilder()
                .header("Authorization", Credentials.basic(apiClouderaProperties.getUsername(), apiClouderaProperties.getPassword()));
            Request newRequest = requestBuilder.build();
            log.debug("Request URL: {}", newRequest.url());
            return chain.proceed(newRequest);
        });
        return builder.build();
    }

}
