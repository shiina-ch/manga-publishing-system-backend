package group1.com.MangaSystemAndManagement.dto.request;

public class AccountLoginRequest {

    private String email;
    private String password;

    public AccountLoginRequest() {}

    public AccountLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
