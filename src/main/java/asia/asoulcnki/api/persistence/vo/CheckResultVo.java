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
    /**
     * 相似度
     */
	@JsonProperty("rate")
	public float allSimilarity;

    /**
     * 起始时间
     */
	@JsonProperty("start_time")
	private int startTime;

    /**
     * 截止时间
     */
    @JsonProperty("end_time")
	private int endTime;

    /**
     * 相似评论列表
     */
	@JsonProperty("related")
	private List<RelatedReply> related;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RelatedReply {
        // 相似度
		private Float rate;

        // 评论
		private Reply reply;

        // 评论链接
		@JsonProperty("reply_url")
		private String replyUrl;

	}

}
