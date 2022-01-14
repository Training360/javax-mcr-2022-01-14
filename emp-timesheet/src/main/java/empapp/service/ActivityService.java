package empapp.service;

import empapp.dto.ActivityDto;
import empapp.dto.CreateActivityCommand;
import empapp.dto.SummaryDto;
import empapp.entities.Activity;
import empapp.entities.Employee;
import empapp.repository.ActivityRepository;
import empapp.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class ActivityService {

    private EmployeeRepository employeeRepository;

    private ActivityRepository activityRepository;

    private ActivityMapper activityMapper;

    public ActivityDto createActivity(long employeeId, CreateActivityCommand command) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + employeeId));
        Activity activity = new Activity(employee, command.getType(), command.getDate(), command.getHour());
        activityRepository.save(activity);
        return activityMapper.toDto(activity);
    }

    public SummaryDto getSummaryForMonth(long eid, String month) {
        if (!month.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        LocalDate start = LocalDate.of(Integer.parseInt(month.substring(0, 4)), Integer.parseInt(month.substring(5, 7)), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return activityRepository.getSummaryForMonth(eid, start, end);
    }
}
