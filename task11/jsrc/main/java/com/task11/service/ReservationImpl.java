package com.task11.service;

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
import com.google.gson.Gson;
import com.task11.DTO.ReservationDTO;
import com.task11.DTO.ReservationsDTO;
import com.task11.DTO.TableDTO;
import com.task11.DTO.TablesDTO;
import com.task11.model.Reservation;

import java.io.IOException;
import java.util.*;

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

    public APIGatewayProxyResponseEvent saveReservation(String requestBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Reservation reservation = objectMapper.readValue(requestBody, Reservation.class);


        TablesDTO tablesDTO = tablesImpl.getAllTables();
        TableDTO tableDTO = tablesDTO.getTables().stream()
                .filter(t -> t.getNumber() == reservation.getTableNumber())
                .findFirst()
                .orElse(null);

        if (tableDTO == null) {
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(Collections.singletonMap("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"))
                    .withHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
                    .withHeaders(Collections.singletonMap("Access-Control-Allow-Methods", "*"))
                    .withHeaders(Collections.singletonMap("Accept-Version", "*"))
                    .withStatusCode(400).withBody("table null");
        }

        ReservationsDTO reservationsForTable = getReservations();

        List<Reservation> reservations = reservationsForTable.fromReservationDTO(reservationsForTable.getReservationsDTO());

        for (Reservation existingReservation : reservations) {
            if (hasOverlap(reservation, existingReservation)) {
                return new APIGatewayProxyResponseEvent()
                        .withHeaders(Collections.singletonMap("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"))
                        .withHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
                        .withHeaders(Collections.singletonMap("Access-Control-Allow-Methods", "*"))
                        .withHeaders(Collections.singletonMap("Accept-Version", "*"))
                        .withStatusCode(400).withBody("Conflicting reservation");
            }
        }
        reservation.setId(UUID.randomUUID().toString());
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(getAmazonDynamoDB());
        dynamoDBMapper.save(reservation);

        Gson gson = new Gson();
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("reservationId", reservation.getId());
        return new APIGatewayProxyResponseEvent()
                .withHeaders(Collections.singletonMap("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"))
                .withHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
                .withHeaders(Collections.singletonMap("Access-Control-Allow-Methods", "*"))
                .withHeaders(Collections.singletonMap("Accept-Version", "*"))
                .withStatusCode(200).withBody(gson.toJson(responseMap));
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

    public APIGatewayProxyResponseEvent getAllReservations() {
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

        List<Reservation> reservations = ReservationsDTO.fromReservationDTO(reservationsDTO.getReservationsDTO());
        Gson gson = new Gson();
        Map<String, List<Reservation>> responseMap = new HashMap<>();
        responseMap.put("reservations", reservations);

        return new APIGatewayProxyResponseEvent()
                .withHeaders(Collections.singletonMap("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"))
                .withHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
                .withHeaders(Collections.singletonMap("Access-Control-Allow-Methods", "*"))
                .withHeaders(Collections.singletonMap("Accept-Version", "*"))
                .withStatusCode(200).withBody(gson.toJson(responseMap));
    }
}
