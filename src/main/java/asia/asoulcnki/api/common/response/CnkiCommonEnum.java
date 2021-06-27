package asia.asoulcnki.api.common.response;

public enum CnkiCommonEnum implements BaseErrorInfoInterface {
	// 数据操作错误定义
	SUCCESS(200, "success"), INVALID_REQUEST(400, "bad request"), BODY_NOT_MATCH(400, "invalid request format"), SIGNATURE_NOT_MATCH(401, "signature not match"), NOT_FOUND(404, "resource not found"), INTERNAL_SERVER_ERROR(500, "internal server error!"), SERVER_BUSY(503, "server is under strong traffic, please retry later"), NO_TOKEN(401, "No jwt token presents"), AUTH(401, "JWT AUTH ERROR"), AUTH_USER_NOT_FOUND(401, "JWT AUTH ERROR"),

	/* Article */
	ARTICLE_NOT_FOUND(10000, "the article not exists"),

	Unknown(400, "unknown"),

	ARTICLE_SEG_EMPTY(10001, "the article segment is empty"),

	/* Comment*/
	REF_COMMENT_NOT_FOUND(15000, "ref comment not found"),

	/* Translation*/
	TRANS_PARAM_ERROR(20000, "translation param error"),

	TRANS_ARTICLE_NOT_EXISTS(20001, "article to translate not exist"),

	TRANS_NOT_EXIST(20002, "trans not exits"),

	NO_PERMISSION_TO_DELETE(20003, "no permission to can delete"),

	TRANS_SEG_STATS_NOT_EXISTS(20004, "trans seg stats not exists"),

	TRANS_SEG_NOT_EXISTS(20005, "trans seg not exists"),

	TRANS_USER_NOT_FOUND(20006, "trans uploader not found"),

	TRANS_SEG_NUMBER_ERROR(20007, "trans segment number error"),

	TRANS_TEXT_SEG_NOT_EXISTS(20008, "trans text seg not exists"),

	// END
	;

	/**
	 * 错误码
	 */
	private int resultCode;

	/**
	 * 错误描述
	 */
	private String resultMsg;

	CnkiCommonEnum(int resultCode, String resultMsg) {
		this.resultCode = resultCode;
		this.resultMsg = resultMsg;
	}

	@Override
	public int getResultCode() {
		return resultCode;
	}

	@Override
	public String getResultMsg() {
		return resultMsg;
	}

}