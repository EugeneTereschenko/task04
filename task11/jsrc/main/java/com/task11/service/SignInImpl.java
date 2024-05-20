package com.task11.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



public class SignInImpl {

    private CognitoIdentityProviderClient cognitoClient;

    public SignInImpl() {
        cognitoClient = CognitoIdentityProviderClient.create();
    }

    public APIGatewayProxyResponseEvent handleSignIn(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent) {

        JSONParser parser = new JSONParser();
        JSONObject bodyJson = null;
        try {
            bodyJson = (JSONObject) parser.parse(apiGatewayProxyRequestEvent.getBody());
        } catch (org.json.simple.parser.ParseException e) {
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(Collections.singletonMap("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"))
                    .withHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
                    .withHeaders(Collections.singletonMap("Access-Control-Allow-Methods", "*"))
                    .withHeaders(Collections.singletonMap("Accept-Version", "*"))
                    .withStatusCode(400).withBody("Sign-in failed");
        }


        String email = (String) bodyJson.get("email");
        String password = (String) bodyJson.get("password");
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", email);
        authParameters.put("PASSWORD", password);

        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .clientId(getClientId())
                .userPoolId(getPoolId())
                .authParameters(authParameters)
                .build();
        try {
            AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);
            if (authResponse.sdkHttpResponse().statusCode() == 200) {
                String accessToken = authResponse.authenticationResult().idToken();
                return new APIGatewayProxyResponseEvent()
                        .withHeaders(Collections.singletonMap("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"))
                        .withHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
                        .withHeaders(Collections.singletonMap("Access-Control-Allow-Methods", "*"))
                        .withHeaders(Collections.singletonMap("Accept-Version", "*"))
                        .withStatusCode(200).withBody("{\"accessToken\":\"" + accessToken + "\"}");
            } else {
                return new APIGatewayProxyResponseEvent()
                        .withHeaders(Collections.singletonMap("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"))
                        .withHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
                        .withHeaders(Collections.singletonMap("Access-Control-Allow-Methods", "*"))
                        .withHeaders(Collections.singletonMap("Accept-Version", "*"))
                        .withStatusCode(400).withBody("Sign-in failed");
            }
        } catch (CognitoIdentityProviderException e) {
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(Collections.singletonMap("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"))
                    .withHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
                    .withHeaders(Collections.singletonMap("Access-Control-Allow-Methods", "*"))
                    .withHeaders(Collections.singletonMap("Accept-Version", "*"))
                    .withStatusCode(400).withBody("Sign-in failed");
        }
    }

    private String getClientId() {
        ListUserPoolClientsRequest listUserPoolClientsRequest = ListUserPoolClientsRequest.builder()
                .userPoolId(getPoolId())
                .build();
        ListUserPoolClientsResponse listUserPoolClientsResponse = getCognitoIdentityProviderClient().listUserPoolClients(listUserPoolClientsRequest);
        return listUserPoolClientsResponse.userPoolClients().get(0).clientId();
    }

    private String getPoolId() {
        String userPoolName = "cmtr-24c2b942-simple-booking-userpool";
        ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder()
                .maxResults(10)
                .build();
        ListUserPoolsResponse listUserPoolsResponse = getCognitoIdentityProviderClient().listUserPools(listUserPoolsRequest);
        return listUserPoolsResponse.userPools().stream()
                .filter(pool -> userPoolName.equals(pool.name()))
                .findFirst()
                .map(UserPoolDescriptionType::id)
                .orElse(null);
    }

    private CognitoIdentityProviderClient getCognitoIdentityProviderClient() {
        if (cognitoClient == null) {
            this.cognitoClient = CognitoIdentityProviderClient.builder()
                    .region(Region.EU_CENTRAL_1)
                    .build();
        }
        return cognitoClient;
    }
}
