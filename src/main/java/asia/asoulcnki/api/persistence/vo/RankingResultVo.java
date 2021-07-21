package asia.asoulcnki.api.persistence.vo;

import asia.asoulcnki.api.persistence.entity.Reply;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingResultVo {
	private List<Reply> replies;

	@JsonProperty("all_count")
	private int allCount;

	@JsonProperty("start_time")
	private int startTime;

	@JsonProperty("end_time")
	private int endTime;
}
