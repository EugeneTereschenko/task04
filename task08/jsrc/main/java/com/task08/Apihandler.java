package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;


@LambdaHandler(lambdaName = "apihandler",
	roleName = "apihandler-role",
	layers = { "sdk-layer" }
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = { "lib/commons-lang3-3.14.0.jar" },
		runtime = DeploymentRuntime.JAVA11,
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class Apihandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		try {

			StringUtils.isNotBlank("qw");

			double latitude = 50.4375;
			double longitude = 30.5;

			String weatherData = OpenMeteoClient.getWeatherForecast(latitude, longitude);

			return response
					.withStatusCode(200)
					.withBody(weatherData);
		}
		catch (IOException | NumberFormatException e) {
			return response
					.withStatusCode(500)
					.withBody("Error: " + e.getMessage());
		}
	}
}
