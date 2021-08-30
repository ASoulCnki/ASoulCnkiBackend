package asia.asoulcnki.api.persistence.param;

import asia.asoulcnki.api.enums.ASoulRoleEnum;
import lombok.Data;

@Data
public class QueryRandomLoveLetterParam {
    // 谁的小作文，null 表示所有人，前端传 AVA, DIANA 等枚举名
    private ASoulRoleEnum role;
    // 随机几篇小作文
    private int limit = 10;
    // 点赞大于多少
    private int likeNum = 50;
    // 字数大于多少
    private int length = 30;
}
