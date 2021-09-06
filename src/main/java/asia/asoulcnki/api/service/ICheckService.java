package asia.asoulcnki.api.service;

import asia.asoulcnki.api.persistence.vo.CheckResultVo;

public interface ICheckService {
    /**
     * 查重
     *
     * @param text 小作文文本
     * @return 查重结果 ViewObject
     */
    CheckResultVo check(String text);
}
