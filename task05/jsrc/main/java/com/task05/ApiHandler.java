package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.task05.DTO.Event;
import com.task05.DTO.RequestDTO;
import com.task05.DTO.ResponseDTO;
import org.apache.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role"
		//isPublishVersion = true,
		//aliasName = "${lambdas_alias_name}",
		//logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(name = "Events-test", resourceType = ResourceType.DYNAMODB_TABLE)
public class ApiHandler implements RequestHandler<RequestDTO, ResponseDTO> {

	private static final String TABLE_NAME = "cmtr-24c2b942-Events-test";
	private final AmazonDynamoDB client = getAmazonDynamoDBClient();

	public ResponseDTO handleRequest(RequestDTO request, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log(request.toString());																																																																																																																																																													
		Event eventObject = createEventObject(request);																																																																																																																																																																																																																																																																																																																																																																																			
		PutItemRequest putItemRequest = getPutItemRequest(eventObject);
		client.putItem(putItemRequest);
		ResponseDTO responseDto = createResponseDTO(eventObject);
		logger.log(responseDto.toString());
		return responseDto;
	}

	private Event createEventObject(RequestDTO request) {
		return new Event(UUID.randomUUID().toString(), request.getPrincipalId(),
				getCurrentDateTime(), request.getContent());
	}

	private PutItemRequest getPutItemRequest(Event event) {
		PutItemRequest putItemRequest = new PutItemRequest();
		Map<String, AttributeValue> itemAttributes = new HashMap<>();

		itemAttributes.put("id", new AttributeValue().withS(event.getId()));
		itemAttributes.put("principalId", new AttributeValue().withN(String.valueOf(event.getPrincipalId())));
		itemAttributes.put("createdAt", new AttributeValue().withS(event.getCreatedAt()));
		itemAttributes.put("body", new AttributeValue().withM(transformContentEntryToAttributeValue(event.getBody())));

		putItemRequest.withTableName(TABLE_NAME).setItem(itemAttributes);
		return putItemRequest;
	}

	private Map<String, AttributeValue> transformContentEntryToAttributeValue(Map<String, String> body) {
		Map<String, AttributeValue> bodyAttributes = new HashMap<>();
		body.forEach((key, value) -> bodyAttributes.put(key, new AttributeValue(value)));
		return bodyAttributes;
	}

	private ResponseDTO createResponseDTO(Event event) {
		return new ResponseDTO()
				.withStatusCode(HttpStatus.SC_CREATED)
				.withEvent(event);
	}

	private AmazonDynamoDB getAmazonDynamoDBClient() {
		return AmazonDynamoDBClient.builder().build();
	}

	public String getCurrentDateTime() {
		return Instant.now().toString();
	}
}
