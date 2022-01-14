package empapp.service;

import empapp.dto.CreateEmployeeCommand;
import empapp.dto.EmployeeDto;
import empapp.dto.UpdateEmployeeCommand;
import empapp.entities.Employee;
import empapp.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EmployeeService {

    private EmployeeRepository employeeRepository;

    private EmployeeMapper employeeMapper;

    public EmployeeDto createEmployee(CreateEmployeeCommand command) {
        Employee employee = new Employee(command.getEid(), command.getName());
        employeeRepository.save(employee);
        return employeeMapper.toDto(employee);
    }

    public List<EmployeeDto> listEmployees() {
        return employeeMapper.toDto(employeeRepository.findAll());
    }

    public EmployeeDto findEmployeeById(long id) {
        return employeeMapper.toDto(employeeRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Employee not found with id: " + id)));
    }

    @Transactional
    public EmployeeDto updateEmployee(long id, UpdateEmployeeCommand command) {
        Employee employeeToModify = employeeRepository.getById(id);
        employeeToModify.setName(command.getName());
        return employeeMapper.toDto(employeeToModify);
    }

    public void deleteEmployee(long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + id));
        employeeRepository.delete(employee);
    }

    public Optional<EmployeeDto> findEmployeeByEid(long employeeId) {
        return employeeRepository.findEmployeeByEid(employeeId).stream().map(employeeMapper::toDto).findAny();
    }
}
