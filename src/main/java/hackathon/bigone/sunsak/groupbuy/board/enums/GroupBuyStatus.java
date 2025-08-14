package hackathon.bigone.sunsak.groupbuy.board.enums;
import lombok.Getter;

@Getter
public enum GroupBuyStatus {
    RECRUITING("모집중"),  // 모집중
    COMPLETED("모집 완료");

    private final String displayName;

    GroupBuyStatus(String displayName) {
        this.displayName = displayName;
    }
}