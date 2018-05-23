package com.example.reservationclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.stream.Collectors;

@EnableBinding(Source.class)
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }
}

@RestController
@RequestMapping("/reservations")
class ReservationApiGatewayRestController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Output(Source.OUTPUT)
    private MessageChannel messageChannel;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @RequestMapping(method = RequestMethod.POST)
    public void write(@RequestBody Reservation reservation) {
        System.out.println("Inside write");
        this.messageChannel.send(MessageBuilder.withPayload(reservation.getReservationName()).build());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/names")
    public Collection<String> getReservationNames() {


        ParameterizedTypeReference<Resources<Reservation>> ptr =
                new ParameterizedTypeReference<Resources<Reservation>>() {
                };

        ResponseEntity<Resources<Reservation>> exchange = this.restTemplate.
                exchange("http://reservation-service/reservations",
                        HttpMethod.GET,
                        null,
                        ptr);

        return exchange.getBody().getContent().stream()
                .map(Reservation::getReservationName).collect(Collectors.toList());
    }

    ;


}

class Reservation {
    private long id;
    private String reservationName;

    public long getId() {
        return id;
    }

    public String getReservationName() {
        return reservationName;
    }
}