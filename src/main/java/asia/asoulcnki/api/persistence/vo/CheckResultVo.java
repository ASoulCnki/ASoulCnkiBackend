package asia.asoulcnki.api.persistence.vo;

import asia.asoulcnki.api.persistence.entity.Reply;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckResultVo {
	@JsonProperty("rate")
	public float allSimilarity;
	@JsonProperty("start_time")
	private int startTime;
	@JsonProperty("end_time")
	private int endTime;
	@JsonProperty("related")
	private List<RelatedReply> related;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RelatedReply {
		private Float rate;

		private Reply reply;

		@JsonProperty("reply_url")
		private String replyUrl;

	}

}
