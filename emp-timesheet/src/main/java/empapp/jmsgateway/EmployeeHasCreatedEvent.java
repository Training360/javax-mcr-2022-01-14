package empapp.jmsgateway;

import lombok.Data;

@Data
public class EmployeeHasCreatedEvent {

    private String name;

    private long eid;
}
