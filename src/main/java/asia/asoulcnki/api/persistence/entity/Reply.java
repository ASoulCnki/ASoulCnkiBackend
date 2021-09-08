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

    /**
     * 评论 id
     */
    @TableId(value = "rpid")
    @JsonProperty("rpid")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rpid;

    /**
     * 动态类型 id
     * 1:      视频
     * 11/17:  文字动态
     * 12:     专栏
     */
    @JsonProperty("type_id")
    private int typeId;

    /**
     * 动态 id
     */
    @JsonProperty("dynamic_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private long dynamicId;

    /**
     * 发布评论者 id
     */
    @JsonProperty("mid")
    private int mid;

    /**
     * 动态发布者 id
     */
    @JsonProperty("uid")
    private int uid;

    /**
     * 视频原稿件 id
     */
    @JsonProperty("oid")
    @JsonSerialize(using = ToStringSerializer.class)
    private long oid;

    /**
     * 评论创建时间
     */
    @JsonProperty("ctime")
    private int ctime;

    /**
     * 评论者昵称
     */
    @JsonProperty("m_name")
    private String mName;

    /**
     * 评论内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 评论点赞数
     */
    @JsonProperty("like_num")
    private int likeNum;

    /**
     * 原创评论 id
     */
    @JsonProperty("origin_rpid")
    @TableField(exist = false)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long originRpid = -1L;

    /**
     * 相似评论数
     */
    @TableField(exist = false)
    @JsonProperty("similar_count")
    private Integer similarCount = 0;

    /**
     * 累积点赞数
     */
    @TableField(exist = false)
    @JsonProperty("similar_like_sum")
    private Integer similarLikeSum = 0;
}
