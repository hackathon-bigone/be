package hackathon.bigone.sunsak.foodbox.foodbox.entity;

import hackathon.bigone.sunsak.global.base.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name="food_box")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FoodBox extends BaseEntity {
    private Long userId;
    private String name;
    private Integer quantity;
    private LocalDate expiryDate;
}
