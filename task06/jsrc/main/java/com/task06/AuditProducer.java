package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.task06.model.Configuration;
import com.task06.model.NewRecord;
import com.task06.model.UpdateRecord;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
		lambdaName = "audit_producer",
		roleName = "audit_producer-role"
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 10)
@DependsOn(name = "Audit", resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE)
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {
	private final AmazonDynamoDB dynamoDb;

	public AuditProducer() {
		this.dynamoDb = AmazonDynamoDBClientBuilder.standard()
				.withRegion("eu-central-1")
				.build();
	}

	public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {
		System.out.println("Entry of handleRequest method");
		for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
			Configuration configuration = new Configuration(
					record.getDynamodb().getNewImage().get("key").getS(),
					record.getDynamodb().getNewImage().get("value").getS()
			);

			if (record.getEventName().equals("INSERT")) {
				System.out.println("INSERT");

				NewRecord newRecord = new NewRecord(
						configuration.getKey(),configuration
				);
				publishAudit(newRecord);
			} else if (record.getEventName().equals("MODIFY")) {
				System.out.println("MODIFY");

				UpdateRecord updateRecord = new UpdateRecord(
						configuration.getKey(),
						"value",
						record.getDynamodb().getOldImage().get("value").getS(),
						record.getDynamodb().getNewImage().get("value").getS()
				);
				publishAudit(updateRecord);
			}
		}
		return null;
	}

	public void publishAudit(NewRecord newRecord) {
		Map<String, AttributeValue> item = new HashMap<>();
		item.put("id", new AttributeValue().withS(newRecord.getId()));
		item.put("itemKey", new AttributeValue().withS(newRecord.getItemKey()));
		item.put("modificationTime", new AttributeValue().withS(newRecord.getModificationTime()));
		item.put("newValue", new AttributeValue().withM(new HashMap<String, AttributeValue>() {{
			put("key", new AttributeValue().withS(newRecord.getNewValue().getKey()));
			put("value", new AttributeValue().withS(newRecord.getNewValue().getValue()));
		}}));
		dynamoDb.putItem("cmtr-24c2b942-Audit-test", item);
	}

	public void publishAudit(UpdateRecord updateRecord) {
		Map<String, AttributeValue> item = new HashMap<>();
		item.put("id", new AttributeValue().withS(updateRecord.getId()));
		item.put("itemKey", new AttributeValue().withS(updateRecord.getItemKey()));
		item.put("modificationTime", new AttributeValue().withS(updateRecord.getModificationTime()));
		item.put("updatedAttribute", new AttributeValue().withS(updateRecord.getUpdatedAttribute()));
		item.put("oldValue", new AttributeValue().withS(updateRecord.getOldValue()));
		item.put("newValue", new AttributeValue().withS(updateRecord.getNewValue()));
		dynamoDb.putItem("cmtr-24c2b942-Audit-test", item);
	}
}
