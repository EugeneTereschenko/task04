package com.task10.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.DTO.ReservationDTO;
import com.task10.DTO.ReservationsDTO;
import com.task10.DTO.TableDTO;
import com.task10.DTO.TablesDTO;
import com.task10.model.Reservation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReservationImpl {

    private Regions REGION = Regions.EU_CENTRAL_1;
    private final String RESERVATION_DB_TABLE_NAME = "cmtr-24c2b942-Reservations-test";

    private AmazonDynamoDB amazonDynamoDB;
    private TablesImpl tablesImpl = new TablesImpl();

    private AmazonDynamoDB getAmazonDynamoDB() {
        if (amazonDynamoDB == null) {
            this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(REGION)
                    .build();
        }
        return amazonDynamoDB;
    }

    public ReservationDTO saveReservation(String requestBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Reservation reservation = objectMapper.readValue(requestBody, Reservation.class);


        TablesDTO tablesDTO = tablesImpl.getAllTables();
        TableDTO tableDTO = tablesDTO.getTables().stream()
                .filter(t -> t.getNumber() == reservation.getTableNumber())
                .findFirst()
                .orElse(null);

        if (tableDTO == null) {
            throw new IllegalArgumentException("Attempting to reserve a non-existent table");
        }

        ReservationsDTO reservationsForTable = getReservations();

        List<Reservation> reservations = reservationsForTable.fromReservationDTO(reservationsForTable.getReservationsDTO());

        for (Reservation existingReservation : reservations) {
            if (hasOverlap(reservation, existingReservation)) {
                throw new IllegalArgumentException("Conflicting reservation");
            }
        }
        reservation.setId(UUID.randomUUID().toString());
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(getAmazonDynamoDB());
        dynamoDBMapper.save(reservation);

        return new ReservationDTO(reservation.getId(), 0, "", "", "", "", "");
    }

    private boolean hasOverlap(Reservation newReservation, Reservation existingReservation) {
        return newReservation.getTableNumber() == existingReservation.getTableNumber()
                && newReservation.getDate().equals(existingReservation.getDate())
                && (newReservation.getSlotTimeStart().compareTo(existingReservation.getSlotTimeStart()) <= 0
                && newReservation.getSlotTimeEnd().compareTo(existingReservation.getSlotTimeEnd()) >= 0);
    }

    public ReservationsDTO getReservations() {
        ScanRequest scanRequest = new ScanRequest().withTableName(RESERVATION_DB_TABLE_NAME);
        ScanResult result = getAmazonDynamoDB().scan(scanRequest);
        ReservationsDTO reservationsDTO = new ReservationsDTO();

        for (Map<String, AttributeValue> item : result.getItems()) {
            ReservationDTO reservationDTO = new ReservationDTO();
            reservationDTO.setId(item.get("id").getN());
            reservationDTO.setTableNumber(Integer.parseInt(item.get("tableNumber").getN()));
            reservationDTO.setClientName(item.get("clientName").getS());
            reservationDTO.setPhoneNumber(item.get("phoneNumber").getS());
            reservationDTO.setDate(item.get("date").getS());
            reservationDTO.setSlotTimeStart(item.get("slotTimeStart").getS());
            reservationDTO.setSlotTimeEnd(item.get("slotTimeEnd").getS());
            reservationsDTO.getReservationsDTO().add(reservationDTO);
        }
        return reservationsDTO;
    }
}
