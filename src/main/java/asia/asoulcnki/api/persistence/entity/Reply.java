package asia.asoulcnki.api.persistence.entity;

import asia.asoulcnki.api.enums.ASoulRoleEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

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

    @JsonProperty("uid")
    private int uid;

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

    /**
     * 得到小作文里某角色出现次数
     *
     * @param roleEnum
     * @return
     */
    public int getLetterRoleRepeatNum(ASoulRoleEnum roleEnum) {
        if (StringUtils.isEmpty(content)) {
            return 0;
        }
        if (roleEnum == null) {
            return 0;
        }
        return content.split(roleEnum.getName()).length - 1;
    }

    /**
     * 当点赞大于指定数，字数大于指定数时，我们认为这是一篇小作文
     *
     * @return
     */
    public boolean isLoveLetter(int likeNum, int length) {
        // 基础条件
        boolean baseCond = this.likeNum > likeNum && this.content.length() > length;
        // 附加条件，如向晚的小作文里经常有 "向晚不是"，"阿八" 等关键字，一般是撕逼，要排除。如珈乐也有请假等大量干扰选项
        // TODO: 优化一下排除算法，但这需要更多数据，同时要考虑到性能
        // TODO: 一个想法：单独靠算法排除掉干扰选项是很难做完善的，一般来说，au们看到小作文才会去查重。如果在查重时，
        //  将匹配的小作文查重次数 +1，之后小作文就从如 查重次数 > 10 的小作文里随机挑选，这样可以保证小作文是比较准确的，而不是骂战留言之类的
        List<String> keywords = Arrays.asList("阿八", "小粒", "请假", "切片", "道歉", "出问题", "解释", "挖掘机", "复盘", "节奏", "运营", "路人", "贵物", "笑嘻");
        boolean extraCond = keywords.stream().noneMatch(k -> content.contains(k));
        return baseCond && extraCond;
    }

    public boolean isLoveLetter() {
        return isLoveLetter(50, 30);
    }

    /**
     * 是否是某个特定的人的小作文？
     *
     * @param roleEnum
     * @param repeatNum
     * @return
     */
    public boolean isSpecRoleLoveLetter(ASoulRoleEnum roleEnum, int repeatNum) {
        // TODO: 是否某个人的小作文算法应该更详细，如有些小作文只出现霓虹甜心和红色高跟鞋而不出现珈乐，待优化
        return getLetterRoleRepeatNum(roleEnum) > repeatNum;
    }

}
