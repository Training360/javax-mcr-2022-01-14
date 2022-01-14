package empapp.controller;

import empapp.dto.CreateEmployeeCommand;
import empapp.dto.EmployeeDto;
import empapp.dto.UpdateEmployeeCommand;
import empapp.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@AllArgsConstructor
public class EmployeeController {

    private EmployeeService employeeService;

    @GetMapping
    public List<EmployeeDto> employees(@RequestParam(required = false) Optional<Long> eid) {
        if (eid.isEmpty()) {
            return employeeService.listEmployees();
        }
        else {
            return employeeService.findEmployeeByEid(eid.get()).stream().toList();
        }
    }

    @GetMapping("/{id}")
    public EmployeeDto findEmployeeById(@PathVariable("id") long id) {
        return employeeService.findEmployeeById(id);
    }

    @PostMapping // nem idempotens
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(description = "creates an employee")
    public EmployeeDto createEmployee(@RequestBody CreateEmployeeCommand command) {
        return employeeService.createEmployee(command);
    }

    @PutMapping("/{id}") // idempotens
    public EmployeeDto updateEmployee(@PathVariable("id") long id, @RequestBody UpdateEmployeeCommand command) {
        return employeeService.updateEmployee(id, command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable("id") long id) {
        employeeService.deleteEmployee(id);
    }

}
