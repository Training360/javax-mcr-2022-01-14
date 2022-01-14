package empapp.timesheet;

import empapp.dto.EmployeeDto;
import empapp.entities.Employee;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Gateway
@EnableConfigurationProperties(TimesheetProperties.class)
public class TimesheetGateway {

    private WebClient webClient;

    public TimesheetGateway(TimesheetProperties properties, WebClient.Builder builder) {
        webClient = builder.baseUrl(properties.getUrl()).build();
    }

    public void createEmployee(Employee employee) {
        webClient
                .post()
                .body(BodyInserters.fromValue(new CreateEmployeeCommand(employee.getName())))
                .retrieve()
                .bodyToMono(EmployeeDto.class)
                .log()
                .block();
    }
}
