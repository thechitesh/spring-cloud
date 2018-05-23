package com.example.reservationservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.AttributeOverride;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@EnableBinding(Sink.class)
@IntegrationComponentScan
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@RefreshScope
@RestController
class MessageRestConroller{


    @Value("${message}")
    private String message;

    @RequestMapping("/message")
    String msg(){
        return this.message;
    }

}

@MessageEndpoint
class ReservationMessageAccepter{

	@Autowired
	private ReservationRepository reservationRepository;

	@ServiceActivator(inputChannel = Sink.INPUT)
	public void accept(String rn){
		System.out.println("Here in accept");
		this.reservationRepository.save(new Reservation(rn));
	}


}


@Component
class DummyDataCLR implements CommandLineRunner{

	@Autowired
	private ReservationRepository reservationRepository;

	@Override
	public void run(String... args) throws Exception {
		Stream.of("Dave","Stephen","Nina","Oliver","Stefan","Mark").forEach(name -> reservationRepository.save(new Reservation(name)));
		reservationRepository.findAll().forEach(System.out::println);

		reservationRepository.findByReservationName("Mark").forEach(System.out::println);
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation,Long> {

	//select * from reservations where reservation_name=:rn
    @RestResource(path = "by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);

}

@Entity
class Reservation {

	@Id
	@GeneratedValue
	private Long id;
	private String reservationName;


	public Reservation() {
	}

	public Reservation(String reservationName) {
		this.reservationName = reservationName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}

	@Override
	public String toString() {
		return "Reservation{" +
				"id=" + id +
				", reservationName='" + reservationName + '\'' +
				'}';
	}
}
