package com.kuochan.es.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RestClient config
 *
 * @author beike
 */
@Configuration
public class RestClientConfig {

    private static final String HTTP_SCHEME = "http";

    @Value("${es.cluster-nodes}")
    String[] esAddress;

    @Bean
    public RestHighLevelClient restHighLevelClient(@Autowired RestClientBuilder restClientBuilder) {
        restClientBuilder.setMaxRetryTimeoutMillis(60000);
        return new RestHighLevelClient(restClientBuilder);
    }

    @Bean
    public RestClientBuilder restClientBuilder() {
        List<HttpHost> httpHostList = new ArrayList<>();
        HttpHost[] httpHosts = new HttpHost[esAddress.length];
        for (String address : esAddress) {
            String[] ipAndPort = address.split(":");
            httpHostList.add(new HttpHost(ipAndPort[0], Integer.valueOf(ipAndPort[1]), HTTP_SCHEME));
        }
        return RestClient.builder(httpHostList.toArray(httpHosts)).setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectionRequestTimeout(60000);
            requestConfigBuilder.setConnectTimeout(5000);
            requestConfigBuilder.setSocketTimeout(60000);
            return requestConfigBuilder;
        }).setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(500);
            httpClientBuilder.setMaxConnPerRoute(100);
            return httpClientBuilder;
        }).setMaxRetryTimeoutMillis(5 * 60 * 1000);
    }
}