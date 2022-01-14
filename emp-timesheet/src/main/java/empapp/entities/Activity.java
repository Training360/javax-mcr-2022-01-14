package empapp.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    private String type;

    private LocalDate date;

    private int hour;

    public Activity(Employee employee, String type, LocalDate date, int hour) {
        this.employee = employee;
        this.type = type;
        this.date = date;
        this.hour = hour;
    }
}
