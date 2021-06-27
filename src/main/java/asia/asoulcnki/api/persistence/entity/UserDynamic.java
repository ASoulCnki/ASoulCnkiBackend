package asia.asoulcnki.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDynamic implements Serializable {

	private static final long serialVersionUID = 5731833077679549516L;

	@TableId(value = "dynamic_id")
	private long dynamicId;

	private int typeId;

	private long oid;

	private int status;
}
