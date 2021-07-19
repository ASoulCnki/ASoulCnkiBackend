package asia.asoulcnki.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class Reply implements Serializable {
	private static final long serialVersionUID = 6554064566476229771L;

	@TableId(value = "rpid")
	@JsonProperty("rpid")
	private Long rpid;

	@JsonProperty("type_id")
	private int typeId;

	@JsonProperty("dynamic_id")
	private long dynamicId;

	@JsonProperty("mid")
	private int mid;

	@JsonProperty("oid")
	private long oid;

	@JsonProperty("ctime")
	private int ctime;

	@JsonProperty("m_name")
	private String mName;

	@JsonProperty("content")
	private String content;

	@JsonProperty("like_num")
	private int likeNum;

	@JsonProperty("origin_rpid")
	@TableField(exist = false)
	private Long originRpid = -1L;

	@TableField(exist = false)
	@JsonProperty("similar_count")
	private Integer similarCount = 0;

	@TableField(exist = false)
	@JsonProperty("similar_like_sum")
	private Integer similarLikeSum = 0;
}
