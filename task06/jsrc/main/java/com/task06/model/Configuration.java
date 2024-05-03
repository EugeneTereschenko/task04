package com.task06.model;

import lombok.*;


@Getter
@Setter
@ToString
public class Configuration {
    private String key;
    private String value;

    public Configuration(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
