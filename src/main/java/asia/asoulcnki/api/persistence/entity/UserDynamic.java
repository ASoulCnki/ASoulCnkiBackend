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

    /**
     * 动态 id
     */
    @TableId(value = "dynamic_id")
    @JsonProperty("dynamic_id")
    private Long dynamicId;

    /**
     * 动态类型 id
     * 1:      视频
     * 11/17:  文字动态
     * 12:     专栏
     */
    @JsonProperty("type_id")
    private int typeId;

    /**
     * 视频原稿件 id
     */
    @JsonProperty("oid")
    private long oid;

    /**
     * 评论爬取状态
     * 0: 未爬取
     * 1: 至少爬取完成过一次
     */
    @JsonProperty("status")
    private int status;

    /**
     * 发布动态者 id
     */
    @JsonProperty("user_id")
    private int userID;
}
