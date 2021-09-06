package asia.asoulcnki.api.common;

import asia.asoulcnki.api.common.response.ApiResult;
import asia.asoulcnki.api.common.response.CnkiCommonEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义的业务异常
     *
     * @param req HttpServletRequest 请求
     * @param e   BizException 异常
     * @return ApiResult
     */
    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    public <T> ApiResult<T> bizExceptionHandler(HttpServletRequest req, BizException e) {
        logger.error("Business exception, caused by {} ", e.getErrorMsg());
        e.printStackTrace();
        return ApiResult.error(e.getErrorCode(), e.getErrorMsg());
    }

    /**
     * 处理其他异常
     *
     * @param req HttpServletRequest 请求
     * @param e   BizException 异常
     * @return ApiResult
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public <T> ApiResult<T> exceptionHandler(HttpServletRequest req, Exception e) {
        int errorCode;
        String errorMsg;
        if (e instanceof ConstraintViolationException) {
            errorCode = CnkiCommonEnum.INVALID_REQUEST.getResultCode();
            errorMsg = "invalid input param: " + e.getMessage();
        } else {
            e.printStackTrace();
            errorCode = CnkiCommonEnum.INTERNAL_SERVER_ERROR.getResultCode();
            errorMsg = "internal server error : " + e;
        }
        return ApiResult.error(errorCode, errorMsg);
    }
}
