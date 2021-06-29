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
	private T data;

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
		rb.setData(data);
		return rb;
	}

	/**
	 * 失败
	 */
	public static <T> ApiResult<T> error(BaseErrorInfoInterface errorInfo) {
		ApiResult<T> rb = new ApiResult<>();
		rb.setCode(errorInfo.getResultCode());
		rb.setMessage(errorInfo.getResultMsg());
		rb.setData(null);
		return rb;
	}

	/**
	 * 失败
	 */
	public static <T> ApiResult<T> error(int code, String message) {
		ApiResult<T> rb = new ApiResult<>();
		rb.setCode(code);
		rb.setMessage(message);
		rb.setData(null);
		return rb;
	}

	public static <T> ApiResult<T> error(int code) {
		ApiResult<T> rb = new ApiResult<>();
		rb.setCode(code);
		rb.setData(null);
		return rb;
	}

	/**
	 * 失败
	 */
	public static <T> ApiResult<T> error(String message) {
		ApiResult<T> rb = new ApiResult<>();
		rb.setCode(-1);
		rb.setMessage(message);
		rb.setData(null);
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

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

}