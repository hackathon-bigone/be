package hackathon.bigone.sunsak.accounts.mypage;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeDto {
    private String currentPassword;
    private String newPassword;
    private String repeatPw;
}
