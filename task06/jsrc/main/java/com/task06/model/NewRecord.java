package com.task06.model;

import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.time.ZonedDateTime;



public class NewRecord {
    private String id;
    private String itemKey;
    private String modificationTime;
    private Configuration newValue;

    public NewRecord(String itemKey, Configuration newValue) {
        this.id = String.valueOf(UUID.randomUUID());
        this.itemKey = itemKey;
        this.modificationTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
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

    public Configuration getNewValue() {
        return newValue;
    }

    public void setNewValue(Configuration newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "NewRecord{" +
                "id='" + id + '\'' +
                ", itemKey='" + itemKey + '\'' +
                ", modificationTime='" + modificationTime + '\'' +
                ", newValue=" + newValue +
                '}';
    }

}
