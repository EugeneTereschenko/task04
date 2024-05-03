package com.task05.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestDTO {
    private int principalId;
    private Map<String, String> content;

    @Override
    public String toString() {
        return "RequestDTO{" +
                "principalId=" + principalId +
                ", content=" + content +
                '}';
    }
}
