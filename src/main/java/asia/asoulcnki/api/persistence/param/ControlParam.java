package asia.asoulcnki.api.persistence.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class ControlParam {
	@JsonProperty("secure_key")
	@NotBlank
	private String secureKey;

	@JsonProperty("start_time")
	@Min(0)
	private int startTime;
}
