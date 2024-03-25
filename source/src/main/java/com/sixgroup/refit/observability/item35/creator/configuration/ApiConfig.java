package com.sixgroup.refit.observability.item35.creator.configuration;


import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import lombok.AllArgsConstructor;
import okhttp3.Credentials;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.cert.CertificateException;

@Configuration
@AllArgsConstructor
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
            RftLog.info("Request URL: " + newRequest.url());
            return chain.proceed(newRequest);
        });
        //configureToIgnoreCertificate(builder);
        return builder.build();
    }

    private OkHttpClient.Builder configureToIgnoreCertificate(OkHttpClient.Builder builder) {
        // LOGGER.warn("Ignore Ssl Certificate");
        try {

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            //LOGGER.warn("Exception while configuring IgnoreSslCertificate" + e, e);
        }
        return builder;
    }
}
