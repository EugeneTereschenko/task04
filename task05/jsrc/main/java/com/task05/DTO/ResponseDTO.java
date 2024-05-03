package com.task05.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResponseDTO {

    private int statusCode;
    private Event event;

    public ResponseDTO withStatusCode(int statusCode) {
        this.setStatusCode(statusCode);
        return this;
    }

    public ResponseDTO withEvent(Event content) {
        this.setEvent(content);
        return this;
    }

    @Override
    public String toString() {
        return "ResponseDTO{" +
                "statusCode=" + statusCode +
                ", event=" + event +
                '}';
    }
}
