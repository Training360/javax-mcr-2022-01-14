package empapp.repository;

import empapp.dto.SummaryDto;
import empapp.entities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ActivityRepository extends JpaRepository<Activity, Long> {


    @Query("select new empapp.dto.SummaryDto(COALESCE(sum(a.hour), 0)) from Activity a where a.employee.id = :employeeId and a.date between :start and :end")
    SummaryDto getSummaryForMonth(@Param("employeeId") long employeeId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
