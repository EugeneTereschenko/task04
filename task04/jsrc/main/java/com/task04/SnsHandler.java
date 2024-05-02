package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LambdaHandler(lambdaName = "sns_handler", roleName = "sns_handler-role")
@SnsEventSource(targetTopic = "lambda_topic")
@DependsOn(resourceType = ResourceType.SNS_TOPIC, name = "lambda_topic")
public class SnsHandler implements RequestHandler<SNSEvent, Map<String, Object>> {

	public Map<String, Object> handleRequest(SNSEvent request, Context context) {
		LambdaLogger logger = context.getLogger();

		List<SNSEvent.SNSRecord> records = request.getRecords();
		SNSEvent.SNSRecord snsRecord = records.get(0);

		logger.log(snsRecord.getSNS().getMessage());

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 200);
		resultMap.put("message", "Hello from Lambda");
		return resultMap;
	}
}