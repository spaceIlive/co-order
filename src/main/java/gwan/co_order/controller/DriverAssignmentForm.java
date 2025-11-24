package gwan.co_order.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverAssignmentForm {
    @NotEmpty(message = "기사 이름은 필수입니다.")
    private String driverName;

    @NotEmpty(message = "연락처는 필수입니다.")
    private String driverContact;
}

