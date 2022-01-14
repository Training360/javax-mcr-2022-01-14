package empapp.jmsgateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryHasChangedEvent {

    private long id;

    private long eid;
}
