package asia.asoulcnki.api.common.response;

import com.alibaba.fastjson.JSONObject;

public class ApiResult<T> {

	/**
	 * 响应代码
	 */
	private int code;

	/**
	 * 响应消息
	 */
	private String message;

	/**
	 * 响应结果
	 */
	private T result;

	public ApiResult() {
	}

	public ApiResult(BaseErrorInfoInterface errorInfo) {
		this.code = errorInfo.getResultCode();
		this.message = errorInfo.getResultMsg();
	}

	/**
	 * 成功
	 *
	 * @return
	 */
	public static <T> ApiResult<T> ok() {
		return ok(null);
	}

	/**
	 * 成功
	 *
	 * @param data
	 * @return
	 */
	public static <T> ApiResult<T> ok(T data) {
		ApiResult<T> rb = new ApiResult<>();
		rb.setCode(CnkiCommonEnum.SUCCESS.getResultCode());
		rb.setMessage(CnkiCommonEnum.SUCCESS.getResultMsg());
		rb.setResult(data);
		return rb;
	}

	/**
	 * 失败
	 */
	public static <T> ApiResult<T> error(BaseErrorInfoInterface errorInfo) {
		ApiResult<T> rb = new ApiResult<>();
		rb.setCode(errorInfo.getResultCode());
		rb.setMessage(errorInfo.getResultMsg());
		rb.setResult(null);
		return rb;
	}

	/**
	 * 失败
	 */
	public static <T> ApiResult<T> error(int code, String message) {
		ApiResult<T> rb = new ApiResult<>();
		rb.setCode(code);
		rb.setMessage(message);
		rb.setResult(null);
		return rb;
	}

	/**
	 * 失败
	 */
	public static <T> ApiResult<T> error(String message) {
		ApiResult<T> rb = new ApiResult<>();
		rb.setCode(-1);
		rb.setMessage(message);
		rb.setResult(null);
		return rb;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

}