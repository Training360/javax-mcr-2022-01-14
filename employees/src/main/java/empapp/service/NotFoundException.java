package empapp.service;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

public class NotFoundException extends AbstractThrowableProblem {

    public NotFoundException(String message) {

        super(
                URI.create("employees/employee-not-found"),
                "Not found",
                Status.NOT_FOUND,
                message);
    }
}
