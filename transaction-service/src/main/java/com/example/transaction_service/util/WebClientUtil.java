package com.example.transaction_service.util;



import com.example.transaction_service.exception.DownStreamValidationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;


@Component

public class WebClientUtil {
    private final WebClient webClient;

    public WebClientUtil(WebClient.Builder webClientBuilder){
        this.webClient=webClientBuilder.build();
    }

    // A generic method that handles any type of request and response
    public<T,R> R exchange(
            HttpMethod httpMethod,
            String uri,
            T body,
            Map<String,String> headers,
            Class<R> responseType){
        WebClient.RequestBodySpec requestBodySpec= webClient.method(httpMethod).uri(uri);

        // add headers dynamically
        if(headers!=null){
            headers.forEach(requestBodySpec::header);
        }

        // add body if present
        if(body!=null){
            requestBodySpec.bodyValue(body);
        }

        return requestBodySpec.retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,response->
                        response.bodyToMono(ProblemDetail.class)
                                .flatMap(problemDetail -> Mono.<Throwable>error(new DownStreamValidationException(problemDetail.getDetail())))
                                .switchIfEmpty(Mono.<Throwable> error(new DownStreamValidationException("Client error: No details provided by downstream service")))

                )
                .onStatus(HttpStatusCode::is5xxServerError,response->
                        Mono.<Throwable>error(new RuntimeException("Downstream service unavailable")))
                .bodyToMono(responseType)
                .block();
    }
}
