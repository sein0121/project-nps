package com.project.nps.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;


@Configuration
public class WebClientConfig {

  @Bean
  public WebClient webClient() {
    DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
    factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

    ConnectionProvider provider = ConnectionProvider.builder("http-pool")
        .maxConnections(100)
        .pendingAcquireTimeout(Duration.ofMillis(0))
        .pendingAcquireMaxCount(-1)
        .maxIdleTime(Duration.ofMillis(1000))
        .build();

    HttpClient httpClient = HttpClient.create(provider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

    return WebClient.builder()
        .uriBuilderFactory(factory)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
        .build();
  }
}

