package demo.utils;

import demo.response.ApiResponse;

public class ApiResponseUtil {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Operation successful", data);
    }

    public static ApiResponse<String> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return new ApiResponse<>(statusCode, message, null);
    }

    public static <T> ApiResponse<T> notFound(String entity) {
        return new ApiResponse<>(404, entity + " not found", null);
    }

    public static <T> ApiResponse<T> notFound(String entity, T data) {
        return new ApiResponse<>(404, entity + " not found", data);
    }

    public static ApiResponse<String> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }

    public static ApiResponse<String> internalServerError(String message) {
        return new ApiResponse<>(500, message, null);
    }
}
