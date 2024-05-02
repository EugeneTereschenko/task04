package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sqs_handler", roleName = "sqs_handler-role", timeout = 25)
@SqsTriggerEventSource(targetQueue = "async_queue", batchSize = 1)
@DependsOn(resourceType = ResourceType.SQS_QUEUE, name = "async_queue")
public class SqsHandler implements RequestHandler<SQSEvent, Map<String, Object>> {

	public Map<String, Object> handleRequest(SQSEvent request, Context context) {
		LambdaLogger logger = context.getLogger();
		SQSEvent.SQSMessage message = request.getRecords().get(0);

		logger.log(message.getBody());

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 200);
		resultMap.put("message", "Hello from Lambda");
		return resultMap;
	}
}