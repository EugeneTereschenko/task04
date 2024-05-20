package com.task11.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class TablesDTO {

    private List<TableDTO> tables;

    public TablesDTO() {
        this.tables = new ArrayList<>();
    }
}
