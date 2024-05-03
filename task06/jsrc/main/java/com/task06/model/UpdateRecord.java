package com.task06.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Setter
@ToString
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
}
