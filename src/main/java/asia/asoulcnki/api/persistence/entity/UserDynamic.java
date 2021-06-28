package asia.asoulcnki.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDynamic implements Serializable {

	private static final long serialVersionUID = 5731833077679549516L;

	@TableId(value = "dynamic_id")
	@JsonProperty("dynamic_id")
	private Long dynamicId;

	@JsonProperty("type_id")
	private int typeId;

	@JsonProperty("oid")
	private long oid;

	@JsonProperty("status")
	private int status;
}
