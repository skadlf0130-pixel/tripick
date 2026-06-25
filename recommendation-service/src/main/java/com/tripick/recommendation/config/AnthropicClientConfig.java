package com.tripick.recommendation.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicClientConfig {

    @Bean
    public AnthropicClient anthropicClient(
            @Value("${ai-api.api-key}") String apiKey,
            @Value("${ai-api.base-url}") String baseUrl
    ) {
        return AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
    }
}
