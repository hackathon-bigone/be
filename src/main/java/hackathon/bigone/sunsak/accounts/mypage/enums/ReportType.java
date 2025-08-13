package hackathon.bigone.sunsak.accounts.mypage.enums;

public enum ReportType {
    INAPPROPRIATE_CONTENT("게시판 성격에 부적절한 대화"),
    INSULT("욕설/비하"),
    PORNOGRAPHIC("음란물/불건전한 콘텐츠"),
    IMPERSONATION("사칭/사기");

    private String displayName;
    ReportType(String displayName) {
        this.displayName = displayName;
    }
}
