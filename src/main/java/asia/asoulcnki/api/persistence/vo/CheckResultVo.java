package asia.asoulcnki.api.persistence.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckResultVo {
	@JsonProperty("all_similarity")
	public float allSimilarity;
	@JsonProperty("start_time")
	private int startTime;
	@JsonProperty("end_time")
	private int endTime;
	@JsonProperty("related")
	private List<List<Object>> related;

}
