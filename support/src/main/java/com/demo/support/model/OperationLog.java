package com.demo.support.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 操作日志实体
 */
public class OperationLog implements Serializable {

    private static final long serialVersionUID = -774644434389393644L;

    private String uuid = UUID.randomUUID().toString();

    private OperationLevel level = OperationLevel.INFO;

    private List<String> tags = new ArrayList<>();

    private List<String> annotations = new ArrayList<>();

    private String timestamp;

    private String localIp;

    private boolean success = false;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public OperationLevel getLevel() {
        return level;
    }

    public void setLevel(OperationLevel level) {
        this.level = level;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
            "uuid='" + uuid + '\'' +
            ", level=" + level +
            ", tags=" + tags +
            ", annotations=" + annotations +
            ", timestamp='" + timestamp + '\'' +
            ", localIp='" + localIp + '\'' +
            ", success=" + success +
            '}';
    }
}
