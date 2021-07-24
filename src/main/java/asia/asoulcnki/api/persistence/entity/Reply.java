package asia.asoulcnki.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
	@JsonSerialize(using = ToStringSerializer.class)
	private Long rpid;

	@JsonProperty("type_id")
	private int typeId;

	@JsonProperty("dynamic_id")
	@JsonSerialize(using = ToStringSerializer.class)
	private long dynamicId;

	@JsonProperty("mid")
	private int mid;

	@JsonProperty("oid")
	@JsonSerialize(using = ToStringSerializer.class)
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
	@JsonSerialize(using = ToStringSerializer.class)
	private Long originRpid = -1L;

	@TableField(exist = false)
	@JsonProperty("similar_count")
	private Integer similarCount = 0;

	@TableField(exist = false)
	@JsonProperty("similar_like_sum")
	private Integer similarLikeSum = 0;
}
