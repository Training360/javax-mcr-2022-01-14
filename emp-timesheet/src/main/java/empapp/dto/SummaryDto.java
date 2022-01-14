package empapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryDto {

    private int hours;

    public SummaryDto(long hours) {
        if (hours > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Not fit");
        }
        this.hours = (int) hours;
    }
}
