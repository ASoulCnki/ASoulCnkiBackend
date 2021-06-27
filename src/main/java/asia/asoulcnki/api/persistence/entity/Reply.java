package asia.asoulcnki.api.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Reply {


	@TableId(value = "rpid")
	private long rpid;

	private int typeId;

	private long dynamicId;

	private int mid;

	private long oid;

	private int ctime;

	private String mName;

	private String content;

	private int like_num;

}
