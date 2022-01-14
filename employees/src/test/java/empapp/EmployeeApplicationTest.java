package empapp;

import empapp.dto.CreateEmployeeCommand;
import empapp.dto.EmployeeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
@Sql(statements = "delete from employee")
class EmployeeApplicationTest {

//    @Autowired
//    WebTestClient webClient;

    @LocalServerPort
    int port;

    @Test
    void testCreateAndQuery() {
        WebTestClient webClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port + "/api/employees")
                .build();

        webClient
                .post()
                .body(BodyInserters.fromValue(new CreateEmployeeCommand("John Doe")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EmployeeDto.class)
                .consumeWith(r -> assertEquals(r.getResponseBody().getName(), "John Doe"));
    }
}
