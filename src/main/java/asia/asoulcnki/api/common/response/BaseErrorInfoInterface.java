package asia.asoulcnki.api.common.response;

public interface BaseErrorInfoInterface extends BaseErrorInterface {

    /**
     * 获取报错信息
     *
     * @return String 报错信息
     */
    String getResultMsg();
}
