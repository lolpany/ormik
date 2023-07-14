package lol.lolpany.ormik.regression;

import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class SendHttpRequest extends EnvironmentBoundAction<String> {
    Method method;
    String body;
    String url;
    Map<String, String> headers;

    public SendHttpRequest(int envNumber, Method method, String url, String body, Map<String, String> headers) {
        super(envNumber);
        this.method = method;
        this.body = body;
        this.url = url;
        this.headers = headers;
    }

    @Override
    protected String runOn(Integer envNumber) throws Exception {
        RequestSpecification requestSpecification = given().param("XML",body);
        for (Map.Entry<String, String> header: headers.entrySet()) {
            requestSpecification = requestSpecification.header(header.getKey(), header.getValue());
        }
        return requestSpecification.request(method, url).getBody().prettyPrint();
    }
}
