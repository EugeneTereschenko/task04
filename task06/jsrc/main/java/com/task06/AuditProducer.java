package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "audit_producer",
		roleName = "audit_producer-role"
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 10)
@DependsOn(name = "Audit", resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

	private static final String ITEM_KEY = "itemKey";
	private static final String MODIFICATION_TIME = "modificationTime";
	private static final String UPDATED_ATTRIBUTE = "updatedAttribute";
	private static final String NEW_VALUE = "newValue";
	private static final String OLD_VALUE = "oldValue";
	private static final String ID = "id";
	private static final String VALUE = "value";
	private static final String KEY = "key";
	private static final String INSERT_EVENT = "INSERT";
	private static final String MODIFY_EVENT = "MODIFY";
	private final AmazonDynamoDB client = getAmazonDynamoDBClient();

	private final String AUDIT_TABLE_NAME = "cmtr-24c2b942-Audit-test";

	private LambdaLogger logger;

	public Void handleRequest(DynamodbEvent request, Context context) {
		logger = context.getLogger();
		logger.log("Request" + request.toString());
		logger.log("Request records " + request.getRecords());
		request.getRecords().stream()
				.filter(eventRecord -> INSERT_EVENT.equals(eventRecord.getEventName()) || MODIFY_EVENT.equals(eventRecord.getEventName()))
				.forEach(this::processEvent);
		return null;
	}

	private void processEvent(DynamodbEvent.DynamodbStreamRecord eventRecord) {
		logger.log("Event name " + eventRecord.getEventName());
		PutItemRequest putItemAttributes = getItemRequest(
				eventRecord.getDynamodb().getNewImage(),
				eventRecord.getDynamodb().getOldImage(),
				eventRecord.getEventName());
		logger.log("Put item request " + putItemAttributes.toString());
		putItemAttributes.withTableName(AUDIT_TABLE_NAME);
		PutItemResult putItemResult = client.putItem(putItemAttributes);
		logger.log("Put item result " + putItemResult.toString());
	}

	private PutItemRequest getItemRequest(
			Map<String, AttributeValue> newImage,
			Map<String, AttributeValue> oldImage,
			String eventName
	) {
		PutItemRequest putItemRequest = new PutItemRequest();
		logger.log("new image log");
		logger.log(newImage.toString());

		if (INSERT_EVENT.equals(eventName)) {
			Map<String, AttributeValue> itemValues = new HashMap<>();
			itemValues.put(ID, new AttributeValue().withS(UUID.randomUUID().toString()));
			itemValues.put(ITEM_KEY, new AttributeValue().withS(newImage.get(KEY).getS()));

			String formattedTime = getFormattedTime();
			itemValues.put(MODIFICATION_TIME, new AttributeValue().withS(formattedTime));

			Map<String, AttributeValue> nestedMap = new HashMap<>();
			nestedMap.put(KEY, new AttributeValue().withS(newImage.get(KEY).getS()));
			nestedMap.put(VALUE, new AttributeValue().withS(newImage.get(VALUE).getS()));

			itemValues.put(NEW_VALUE, new AttributeValue().withM(nestedMap));

			putItemRequest.withItem(itemValues);
		} else if (MODIFY_EVENT.equals(eventName)) {
			Map<String, AttributeValue> itemValues = new HashMap<>();
			itemValues.put(ID, new AttributeValue().withS(UUID.randomUUID().toString()));
			itemValues.put(ITEM_KEY, new AttributeValue().withS(newImage.get(KEY).getS()));

			itemValues.put(MODIFICATION_TIME, new AttributeValue().withS(getFormattedTime()));

			itemValues.put(UPDATED_ATTRIBUTE, new AttributeValue().withS(VALUE));
			itemValues.put(OLD_VALUE, new AttributeValue().withS(oldImage.get(VALUE).getS()));
			itemValues.put(NEW_VALUE, new AttributeValue().withS(newImage.get(VALUE).getS()));

			putItemRequest.withItem(itemValues);
		}
		return putItemRequest;
	}

	private static String getFormattedTime() {
		LocalDateTime modificationTime = LocalDateTime.now();
		return modificationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
	}

	private AmazonDynamoDB getAmazonDynamoDBClient() {
		return AmazonDynamoDBClientBuilder.defaultClient();
	}
}
