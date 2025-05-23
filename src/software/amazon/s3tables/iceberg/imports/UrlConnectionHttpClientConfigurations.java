/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package software.amazon.s3tables.iceberg.imports;

import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.util.PropertyUtil;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.http.urlconnection.ProxyConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

class UrlConnectionHttpClientConfigurations {

  private Long httpClientUrlConnectionConnectionTimeoutMs;
  private Long httpClientUrlConnectionSocketTimeoutMs;
  private String proxyEndpoint;

  private UrlConnectionHttpClientConfigurations() {}

  public <T extends AwsSyncClientBuilder> void configureHttpClientBuilder(T awsClientBuilder) {
    UrlConnectionHttpClient.Builder urlConnectionHttpClientBuilder =
        UrlConnectionHttpClient.builder();
    configureUrlConnectionHttpClientBuilder(urlConnectionHttpClientBuilder);
    awsClientBuilder.httpClientBuilder(urlConnectionHttpClientBuilder);
  }

  private void initialize(Map<String, String> httpClientProperties) {
    this.httpClientUrlConnectionConnectionTimeoutMs =
        PropertyUtil.propertyAsNullableLong(
            httpClientProperties, HttpClientProperties.URLCONNECTION_CONNECTION_TIMEOUT_MS);
    this.httpClientUrlConnectionSocketTimeoutMs =
        PropertyUtil.propertyAsNullableLong(
            httpClientProperties, HttpClientProperties.URLCONNECTION_SOCKET_TIMEOUT_MS);
    this.proxyEndpoint =
        PropertyUtil.propertyAsString(
            httpClientProperties, HttpClientProperties.PROXY_ENDPOINT, null);
  }

  @VisibleForTesting
  void configureUrlConnectionHttpClientBuilder(
      UrlConnectionHttpClient.Builder urlConnectionHttpClientBuilder) {
    if (httpClientUrlConnectionConnectionTimeoutMs != null) {
      urlConnectionHttpClientBuilder.connectionTimeout(
          Duration.ofMillis(httpClientUrlConnectionConnectionTimeoutMs));
    }
    if (httpClientUrlConnectionSocketTimeoutMs != null) {
      urlConnectionHttpClientBuilder.socketTimeout(
          Duration.ofMillis(httpClientUrlConnectionSocketTimeoutMs));
    }
    if (proxyEndpoint != null) {
      urlConnectionHttpClientBuilder.proxyConfiguration(
          ProxyConfiguration.builder().endpoint(URI.create(proxyEndpoint)).build());
    }
  }

  public static UrlConnectionHttpClientConfigurations create(
      Map<String, String> httpClientProperties) {
    UrlConnectionHttpClientConfigurations configurations =
        new UrlConnectionHttpClientConfigurations();
    configurations.initialize(httpClientProperties);
    return configurations;
  }
}
