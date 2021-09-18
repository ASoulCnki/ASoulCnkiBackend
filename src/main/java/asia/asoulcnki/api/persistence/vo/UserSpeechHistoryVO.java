package asia.asoulcnki.api.persistence.vo;

import asia.asoulcnki.api.persistence.entity.Reply;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserSpeechHistoryVO implements Serializable {
    private Integer mid;

    private List<Reply> replies;
}
