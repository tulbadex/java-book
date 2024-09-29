package demo.response;

public class ApiResponse<T> {
    private int status_code;
    private String message;
    private T data; // Generic type to hold any type of data (like user details)

    public ApiResponse(int status_code, String message, T data) {
        this.status_code = status_code;
        this.message = message;
        this.data = data;
    }

    // Getters and setters
    public int getStatusCode() {
        return status_code;
    }

    public void setStatusCode(int status_code) {
        this.status_code = status_code;
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
}
