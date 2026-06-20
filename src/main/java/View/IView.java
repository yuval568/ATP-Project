package View;

public interface IView {
    void showMessage(String message);
    void showError(String errorMessage);
    void setLoading(boolean isLoading);
    void clearFields();
    void closeWindow();
}