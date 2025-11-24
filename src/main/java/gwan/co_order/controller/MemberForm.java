package gwan.co_order.controller;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberForm {
    
    @NotEmpty(message = "이름은 필수입니다")
    private String name;
    
    @NotEmpty(message = "비밀번호는 필수입니다")
    private String password;
    
    private String passwordConfirm;
    
    @NotEmpty(message = "주소는 필수입니다")
    private String address;
    
    @NotNull(message = "위도는 필수입니다")
    private Double latitude;
    
    @NotNull(message = "경도는 필수입니다")
    private Double longitude;
    
    // 비밀번호 확인 검증
    @AssertTrue(message = "비밀번호가 일치하지 않습니다")
    public boolean isPasswordMatching() {
        if (password == null || passwordConfirm == null) {
            return true; // null 체크는 @NotEmpty에서 처리
        }
        return password.equals(passwordConfirm);
    }
}
