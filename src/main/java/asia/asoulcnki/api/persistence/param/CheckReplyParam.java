package asia.asoulcnki.api.persistence.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckReplyParam {
	@NotBlank(message = "评论不能为空")
	private String text;
}
