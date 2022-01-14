package empapp.jmsgateway;

import empapp.dto.CreateEmployeeCommand;
import empapp.service.EmployeeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmployeesGateway {

    private JmsTemplate jmsTemplate;

    private EmployeeService employeeService;

    @JmsListener(destination = "employeesQueue")
    public void processMessage(EmployeeHasCreatedEvent event) {
        log.debug("Process EmployeeHasCreatedEvent");
        employeeService.createEmployee(new CreateEmployeeCommand(event.getName(), event.getEid()));
    }

    public void changeSummary(long id, long eid) {
        log.debug("Send SummaryHasChangedEvent");
        jmsTemplate.convertAndSend("timesheetQueue", new SummaryHasChangedEvent(id, eid));
    }

}
