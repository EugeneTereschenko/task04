package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task10.service.ReservationImpl;
import com.task10.service.SignInImpl;
import com.task10.service.SignUpImpl;
import com.task10.service.TablesImpl;

import java.io.IOException;


@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private SignInImpl signInImpl;
    private SignUpImpl signUpImpl;
    private TablesImpl tablesImpl;
    private ReservationImpl reservationImpl;

    public ApiHandler() {
        this.signInImpl = new SignInImpl();
        this.signUpImpl = new SignUpImpl();
        this.tablesImpl = new TablesImpl();
        this.reservationImpl = new ReservationImpl();
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {

        String path = apiGatewayProxyRequestEvent.getPath();
        String httpMethod = apiGatewayProxyRequestEvent.getHttpMethod();
        String requestString = apiGatewayProxyRequestEvent.getBody();
        context.getLogger().log(requestString + " apiGateway ");
        context.getLogger().log(path + " path");
        context.getLogger().log(httpMethod + " httpMethod");


        if (path.equals("/signup") && httpMethod.equals("POST")) {
            context.getLogger().log(requestString + " requestString signup");
            return signUpImpl.handleSignUp(apiGatewayProxyRequestEvent);
        } else if (path.equals("/signin") && httpMethod.equals("POST")) {
            context.getLogger().log(requestString + " requestString signin");
            return signInImpl.handleSignIn(apiGatewayProxyRequestEvent);
        } else if (path.equals("/tables") && httpMethod.equals("GET")) {
            context.getLogger().log(requestString + " requestString get tables");
            return tablesImpl.getTables();
        } else if (path.equals("/tables") && httpMethod.equals("POST")) {
            try {
                return tablesImpl.saveTable(apiGatewayProxyRequestEvent.getBody());
            } catch (IOException e) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(e.toString());
            }
        } else if (path.matches("/tables/\\d+") && httpMethod.equals("GET")) {
            int tableId = Integer.parseInt(apiGatewayProxyRequestEvent.getPathParameters().get("tableId"));
            try {
                return tablesImpl.getTablesById(tableId);
            } catch (Exception e) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(e.toString());
            }
        } else if (path.equals("/reservations") && httpMethod.equals("POST")) {
            try {
                return reservationImpl.saveReservation(apiGatewayProxyRequestEvent.getBody());
            } catch (IOException e) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(e.toString());
            }
        } else if (path.equals("/reservations") && httpMethod.equals("GET")) {
            try {
                return reservationImpl.getAllReservations();
            } catch (Exception e) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(e.toString());
            }
        } else {
            return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody("Error request");
        }
    }
}
