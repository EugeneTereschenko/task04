package com.task05.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String id;
    private int principalId;
    private String createdAt;
    private Map<String, String> body;

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", principalId=" + principalId +
                ", createdAt='" + createdAt + '\'' +
                ", body=" + body +
                '}';
    }
}
