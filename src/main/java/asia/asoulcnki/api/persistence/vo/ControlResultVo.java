package asia.asoulcnki.api.persistence.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControlResultVo {
	@JsonProperty("start_time")
	private int startTime;

	@JsonProperty("end_time")
	private int endTime;
}
