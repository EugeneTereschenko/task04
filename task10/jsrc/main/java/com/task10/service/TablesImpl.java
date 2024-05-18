package com.task10.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.task10.DTO.TableDTO;
import com.task10.DTO.TablesDTO;
import com.task10.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;


public class TablesImpl {

    private Regions REGION = Regions.EU_CENTRAL_1;
    private final String TABLES_DB_TABLE_NAME = "cmtr-24c2b942-Tables-test";
    private static final Logger log = LoggerFactory.getLogger(SignInImpl.class);

    private AmazonDynamoDB amazonDynamoDB;

    private AmazonDynamoDB getAmazonDynamoDB() {
        if (amazonDynamoDB == null) {
            this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(REGION)
                    .build();
        }
        return amazonDynamoDB;
    }

    public APIGatewayProxyResponseEvent getTables() {
        ScanRequest scanRequest = new ScanRequest().withTableName(TABLES_DB_TABLE_NAME);
        ScanResult result = getAmazonDynamoDB().scan(scanRequest);
        TablesDTO tablesDTO = new TablesDTO();
        for (Map<String, AttributeValue> item : result.getItems()) {
            TableDTO tableDTO = new TableDTO();
            tableDTO.setId(Integer.parseInt(item.get("id").getN()));
            tableDTO.setNumber(Integer.parseInt(item.get("number").getN()));
            tableDTO.setPlaces(Integer.parseInt(item.get("places").getN()));
            tableDTO.setIsVip(Boolean.parseBoolean(String.valueOf(item.get("isVip").getBOOL())));
            if (item.containsKey("minOrder")) {
                tableDTO.setMinOrder(Integer.parseInt(item.get("minOrder").getN()));
            }
            tablesDTO.getTables().add(tableDTO);
        }
        log.info( "Tables " + tablesDTO);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(tablesDTO);
        return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("{\"tables\":" + json + "}");


    }

    public APIGatewayProxyResponseEvent saveTable(String requestBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Table table = objectMapper.readValue(requestBody, Table.class);

        Table savedTable = new Table();
        savedTable.setId(table.getId());
        savedTable.setNumber(table.getNumber());
        savedTable.setVip(table.isVip());
        savedTable.setPlaces(table.getPlaces());
        savedTable.setMinOrder(table.getMinOrder());
        DynamoDBMapper dbMapper = new DynamoDBMapper(getAmazonDynamoDB());
        try {
            dbMapper.save(savedTable);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("{\"id\":" + table.getId() + "}");
        } catch (Exception e) {
            System.out.println(e);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("{\"id\":\"" +  new TableDTO(0, 0, 0, false, 0) + "\"}");
        }
    }

    public APIGatewayProxyResponseEvent getTablesById(int id) {
        Gson gson = new Gson();
        try {
            Table response = getTableById(id);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(gson.toJson(response));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(gson.toJson(e.getMessage()));
        }
    }

    public Table getTableById(int tableId) throws Exception {
        DynamoDB dynamoDB = new DynamoDB(getAmazonDynamoDB());
        com.amazonaws.services.dynamodbv2.document.Table dynamoTable = dynamoDB.getTable(TABLES_DB_TABLE_NAME);

        GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", tableId);
        Item item = dynamoTable.getItem(getItemSpec);
        if (item == null) {
            throw new Exception("Table not found");
        }
        System.out.println("Table id:" + tableId);
        System.out.println("Item:" + item);
        Table table = new Table();
        table.setId(item.getInt("id"));
        table.setNumber(item.getInt("number"));
        table.setPlaces(item.getInt("places"));
        table.setVip(item.getNumber("isVip").equals(BigDecimal.ONE));
        if (item.isPresent("minOrder")) {
            table.setMinOrder(item.getInt("minOrder"));
        }
        return table;
    }

    public TablesDTO getAllTables() {
        ScanRequest scanRequest = new ScanRequest().withTableName(TABLES_DB_TABLE_NAME);
        ScanResult result = getAmazonDynamoDB().scan(scanRequest);
        TablesDTO tablesDTO = new TablesDTO();
        for (Map<String, AttributeValue> item : result.getItems()) {
            TableDTO tableDTO = new TableDTO();
            tableDTO.setId(Integer.parseInt(item.get("id").getN()));
            tableDTO.setNumber(Integer.parseInt(item.get("number").getN()));
            tableDTO.setPlaces(Integer.parseInt(item.get("places").getN()));
            tableDTO.setIsVip(Boolean.parseBoolean(String.valueOf(item.get("isVip").getBOOL())));
            if (item.containsKey("minOrder")) {
                tableDTO.setMinOrder(Integer.parseInt(item.get("minOrder").getN()));
            }
            tablesDTO.getTables().add(tableDTO);
        }
        log.info( "Tables " + tablesDTO);
        return tablesDTO;

    }
}
