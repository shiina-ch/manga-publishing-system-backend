package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "items")
public class Item {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 1)
    @NotNull
    @Column(name = "name", nullable = false, length = 1)
    private String name;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", precision = 18, scale = 2)
    private BigDecimal price;


}