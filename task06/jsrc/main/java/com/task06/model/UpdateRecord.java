package com.task06.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


public class UpdateRecord {
    private String id;
    private String itemKey;
    private String modificationTime;
    private String updatedAttribute;
    private String oldValue;
    private String newValue;

    public UpdateRecord(String itemKey, String updatedAttribute, String oldValue, String newValue) {
        this.id = String.valueOf(UUID.randomUUID());
        this.itemKey = itemKey;
        this.modificationTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);;
        this.updatedAttribute = updatedAttribute;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(String modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getUpdatedAttribute() {
        return updatedAttribute;
    }

    public void setUpdatedAttribute(String updatedAttribute) {
        this.updatedAttribute = updatedAttribute;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "UpdateRecord{" +
                "id='" + id + '\'' +
                ", itemKey='" + itemKey + '\'' +
                ", modificationTime='" + modificationTime + '\'' +
                ", updatedAttribute='" + updatedAttribute + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }
}
