package asia.asoulcnki.api.persistence.vo;

import asia.asoulcnki.api.persistence.entity.Reply;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RankingResultVo {
	private List<Reply> replies;
}
