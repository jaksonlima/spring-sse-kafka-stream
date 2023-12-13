package com.br.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka.consumers.sse")
public class KafkaConsumerProperties {

    private String topic;
    private String clientId;
    private String groupId;
    private String sessionTimeoutMs;
    private String defaultApiTimeoutMs;
    private String requestTimeoutMs;
    private String reconnectBackoffMaxMs;
    private String reconnectBackoffMs;
    private String retryBackoffMs;
    private boolean enableAutoCommit;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(String sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public String getDefaultApiTimeoutMs() {
        return defaultApiTimeoutMs;
    }

    public void setDefaultApiTimeoutMs(String defaultApiTimeoutMs) {
        this.defaultApiTimeoutMs = defaultApiTimeoutMs;
    }

    public String getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(String requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public String getReconnectBackoffMaxMs() {
        return reconnectBackoffMaxMs;
    }

    public void setReconnectBackoffMaxMs(String reconnectBackoffMaxMs) {
        this.reconnectBackoffMaxMs = reconnectBackoffMaxMs;
    }

    public String getReconnectBackoffMs() {
        return reconnectBackoffMs;
    }

    public void setReconnectBackoffMs(String reconnectBackoffMs) {
        this.reconnectBackoffMs = reconnectBackoffMs;
    }

    public String getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public void setRetryBackoffMs(String retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }

    public boolean isEnableAutoCommit() {
        return enableAutoCommit;
    }

    public void setEnableAutoCommit(boolean enableAutoCommit) {
        this.enableAutoCommit = enableAutoCommit;
    }

}
