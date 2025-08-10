package hackathon.bigone.sunsak.global.base.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@MappedSuperclass
@Getter
@Setter(AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTime extends BaseEntity {
    @CreatedDate
    private LocalDate createDate;
    @LastModifiedDate
    private LocalDate modifyDate; //LocalDateTime을 LocalDate로 수정

    public void setModified() {
        setModifyDate(LocalDate.now());
    }
}
