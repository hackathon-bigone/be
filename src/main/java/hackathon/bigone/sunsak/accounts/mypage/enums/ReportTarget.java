package hackathon.bigone.sunsak.accounts.mypage.enums;

public enum ReportTarget {
    USER("사용자"),
    POST("게시글");

    private String displayName;
    ReportTarget(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
