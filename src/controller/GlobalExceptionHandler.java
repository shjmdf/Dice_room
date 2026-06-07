package controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleStateError(IllegalStateException e) {
        String msg = e.getMessage();
        if (isAuthError(msg)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(msg));
        }
        if (isForbiddenError(msg)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(msg));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("服务器内部错误"));
    }

    private boolean isAuthError(String msg) {
        if (msg == null) return false;
        return msg.contains("请先登录")
                || msg.contains("登录已过期")
                || msg.contains("缺少登录凭证")
                || msg.contains("登录凭证不能为空")
                || msg.contains("用户状态不可使用");
    }

    private boolean isForbiddenError(String msg) {
        if (msg == null) return false;
        return msg.contains("需要管理员权限")
                || msg.contains("无权")
                || msg.contains("只能操作自己");
    }

    public record ErrorResponse(String error) {
    }
}
