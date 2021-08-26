package asia.asoulcnki.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ASoulRoleEnum {
    AVA(0, "向晚"),
    BELLA(1, "贝拉"),
    CAROL(2, "珈乐"),
    DIANA(3, "嘉然"),
    EILEEN(4, "乃琳");

    @EnumValue
    private int code;

    @JsonValue
    private String name;
}
