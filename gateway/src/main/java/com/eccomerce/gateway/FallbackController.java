package com.eccomerce.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class FallbackController {

    @GetMapping("/fallback/products")
    public ResponseEntity<List<String>> productsFallback(){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(List.of("Oooops! Product Service is unavailable, please try after some time"));
    }

    @GetMapping("/fallback/users")
    public ResponseEntity<List<String>> userFallback(){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(List.of("Oooops! User Service is unavailable, please try after some time"));
    }

    @GetMapping("/fallback/orders")
    public ResponseEntity<List<String>> ordersFallback(){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(List.of("Oooops! Order Service is unavailable, please try after some time"));
    }



}
