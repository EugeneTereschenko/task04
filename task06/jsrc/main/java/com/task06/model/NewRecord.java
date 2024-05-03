package com.task06.model;

import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.time.ZonedDateTime;


@Getter
@Setter
@ToString
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

}
