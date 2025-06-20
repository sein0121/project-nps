package com.project.nps.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientUtil {

  private static final Logger logger = LoggerFactory.getLogger(WebClientUtil.class);

  private final WebClient webClient;

  public WebClientUtil(WebClient.Builder builder) {
    this.webClient = builder.build();
  }

  public <T> T post(String url, org.json.simple.JSONObject jsonObject, Class<T> responseType) {
    try {
      // JSON → x-www-form-urlencoded 형식으로 변환
      MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
      for (Object key : jsonObject.keySet()) {
        String field = (String) key;
        Object value = jsonObject.get(field);

        if (value instanceof org.json.simple.JSONArray) {
          // 배열 처리 → ["a", "b"] → 각각 add
          for (Object v : (org.json.simple.JSONArray) value) {
            formData.add(field, v.toString());
          }
        } else {
          formData.add(field, value == null ? "" : value.toString());
        }
      }

      return webClient.post()
          .uri(url)
          .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
          .header("Accept", MediaType.APPLICATION_JSON_VALUE)
          .body(BodyInserters.fromFormData(formData))
          .retrieve()
          .bodyToMono(responseType)
          .block();

    } catch (Exception e) {
      logger.error("WebClient POST error: {}", e.getMessage());
      throw new RuntimeException("WebClient POST error: " + e.getMessage(), e);
    }
  }

  public <T> T postJson(String url, org.json.simple.JSONObject jsonObject, Class<T> responseType) {
    try {

      return webClient.post()
          .uri(url)
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .header("Accept", MediaType.APPLICATION_JSON_VALUE)
          .body(BodyInserters.fromValue(jsonObject.toJSONString()))
          .retrieve()
          .bodyToMono(responseType)
          .block();

    } catch (Exception e) {
      logger.error("WebClient POST error: {}", e.getMessage());
      throw new RuntimeException("WebClient POST error: " + e.getMessage(), e);
    }
  }

}
