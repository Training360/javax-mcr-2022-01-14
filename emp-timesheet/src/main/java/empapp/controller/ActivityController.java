package empapp.controller;

import empapp.dto.ActivityDto;
import empapp.dto.CreateActivityCommand;
import empapp.dto.SummaryDto;
import empapp.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ActivityController {

    private ActivityService activityService;

    @PostMapping("/api/employees/{employeeId}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityDto createActivity(@PathVariable("employeeId") long employeeId, @RequestBody CreateActivityCommand command) {
        return activityService.createActivity(employeeId, command);
    }

    @GetMapping("/api/employees/{employeeId}/activities/summary/{month}")
    public SummaryDto getSummaryForMonth(@PathVariable("employeeId") long employeeId, @PathVariable("month") String month) {
        return activityService.getSummaryForMonth(employeeId, month);
    }

}
