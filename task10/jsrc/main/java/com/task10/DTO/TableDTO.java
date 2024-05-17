package com.task10.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableDTO {
    private Integer id;
    private Integer number;
    private Integer places;
    private Boolean isVip;
    private Integer minOrder;
}
