package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.LambdaUrlConfig;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@LambdaHandler(lambdaName = "processor",
		roleName = "processor-role",
		layers = { "sdk-layer" }
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = { "lib/jackson-databind-2.12.1.jar", "lib/jackson-annotations-2.12.1.jar", "lib/jackson-core-2.12.1.jar" },
		runtime = DeploymentRuntime.JAVA11,
		artifactExtension = ArtifactExtension.ZIP
)
@DynamoDbTriggerEventSource(targetTable = "Weather", batchSize = 1)
@DependsOn(name = "Weather", resourceType = ResourceType.DYNAMODB_TABLE)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class Processor implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
			.withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
			.build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {
			String weatherData = getWeatherForecast(50.4375, 30.5);
			JsonNode jsonNode = objectMapper.readTree(weatherData);
			String id = UUID.randomUUID().toString();

			Map<String, AttributeValue> forecastData = new HashMap<>();
			forecastData.put("latitude", new AttributeValue().withN(String.valueOf(jsonNode.get("latitude").asDouble())));
			forecastData.put("longitude", new AttributeValue().withN(String.valueOf(jsonNode.get("longitude").asDouble())));
			forecastData.put("generationtime_ms", new AttributeValue().withN(String.valueOf(jsonNode.get("generationtime_ms").asDouble())));
			forecastData.put("utc_offset_seconds", new AttributeValue().withN(String.valueOf(jsonNode.get("utc_offset_seconds").asInt())));
			forecastData.put("timezone", new AttributeValue().withS(jsonNode.get("timezone").asText()));
			forecastData.put("timezone_abbreviation", new AttributeValue().withS(jsonNode.get("timezone_abbreviation").asText()));
			forecastData.put("elevation", new AttributeValue().withN(String.valueOf(jsonNode.get("elevation").asDouble())));

			JsonNode hourlyNode = jsonNode.path("hourly");
			Map<String, AttributeValue> hourlyData = new HashMap<>();

			List<AttributeValue> temperatureList = new ArrayList<>();
			hourlyNode.path("temperature_2m").forEach(temp ->
					temperatureList.add(new AttributeValue().withN(temp.asText()))
			);
			hourlyData.put("temperature_2m", new AttributeValue().withL(temperatureList));

			List<AttributeValue> timeList = new ArrayList<>();
			hourlyNode.path("time").forEach(time ->
					timeList.add(new AttributeValue().withS(time.asText()))
			);
			hourlyData.put("time", new AttributeValue().withL(timeList));

			forecastData.put("hourly", new AttributeValue().withM(hourlyData));

			JsonNode hourlyUnitsNode = jsonNode.path("hourly_units");
			Map<String, AttributeValue> hourlyUnitsData = new HashMap<>();
			hourlyUnitsData.put("temperature_2m", new AttributeValue().withS(hourlyUnitsNode.path("temperature_2m").asText()));
			hourlyUnitsData.put("time", new AttributeValue().withS(hourlyUnitsNode.path("time").asText()));
			forecastData.put("hourly_units", new AttributeValue().withM(hourlyUnitsData));

			Map<String, AttributeValue> item = new HashMap<>();
			item.put("id", new AttributeValue().withS(id));
			item.put("forecast", new AttributeValue().withM(forecastData));

			dynamoDB.putItem(new PutItemRequest().withTableName("cmtr-24c2b942-Weather-test").withItem(item));

			return response
					.withStatusCode(200)
					.withBody("Weather data stored successfully with ID: " + id);
		}
		catch (IOException e) {
			return response
					.withStatusCode(500)
					.withBody("Error: " + e.getMessage());
		}
	}

	private String getWeatherForecast(double latitude, double longitude) throws IOException {
		String baseUrl = "https://api.open-meteo.com/v1/forecast";
		URL url = new URL(baseUrl + "?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.connect();

		int responseCode = conn.getResponseCode();
		if (responseCode != 200) {
			throw new RuntimeException("HttpResponseCode: " + responseCode);
		}
		else {
			Scanner scanner = new Scanner(url.openStream());
			String response = scanner.useDelimiter("\\A").next();
			scanner.close();
			return response;
		}
	}
}
