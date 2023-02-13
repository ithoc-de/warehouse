package de.ithoc.warehouse.external;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class RestClient {

    private final RestTemplate restTemplate;
    private final HttpHeaders authorizationHeader;

    public RestClient(RestTemplate restTemplate, HttpHeaders authorizationHeader) {
        this.restTemplate = restTemplate;
        this.authorizationHeader = authorizationHeader;
    }


    public <T> T get(String resource, Map<String, String> variables, Class<T> clazz) {

        HttpEntity<String> httpEntity = new HttpEntity<>(authorizationHeader);
        ResponseEntity<T> responseEntity = restTemplate.exchange(
                resource, HttpMethod.GET, httpEntity, clazz, variables);

        return responseEntity.getBody();
    }

}
