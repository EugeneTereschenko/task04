package com.task10.DTO;

import com.task10.model.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ReservationsDTO {
    private List<ReservationDTO> reservationsDTO;

    public ReservationsDTO(List<ReservationDTO> reservationsDTO) {
        this.reservationsDTO = reservationsDTO;
    }

    public ReservationsDTO() {
        this.reservationsDTO = new ArrayList<>();
    }

    public static List<ReservationDTO> fromReservationModel(List<Reservation> reservations) {
        return reservations.stream()
                .map(r -> new ReservationDTO(r.getId(), r.getTableNumber(), r.getClientName(), r.getPhoneNumber(), r.getDate(), r.getSlotTimeStart(), r.getSlotTimeEnd()))
                .collect(Collectors.toList());
    }

    public static List<Reservation> fromReservationDTO(List<ReservationDTO> reservationsDTO) {
        return reservationsDTO.stream()
                .map(r -> new Reservation(r.getId(), r.getTableNumber(), r.getClientName(), r.getPhoneNumber(), r.getDate(), r.getSlotTimeStart(), r.getSlotTimeEnd()))
                .collect(Collectors.toList());
    }

}
