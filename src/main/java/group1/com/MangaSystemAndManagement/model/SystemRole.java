package group1.com.MangaSystemAndManagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class SystemRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String roleName;

    @ManyToMany(mappedBy = "systemRole")
    @JsonIgnore
    private List<Account> account;

    public SystemRole() {}

    public SystemRole(long id, String roleName, List<Account> account) {
        this.id = id;
        this.roleName = roleName;
        this.account = account;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public List<Account> getAccount() { return account; }
    public void setAccount(List<Account> account) { this.account = account; }
}
