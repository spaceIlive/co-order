package gwan.co_order.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StoreForm {
    
    @NotEmpty(message = "가게명은 필수입니다")
    private String name;
    
    @NotEmpty(message = "주소는 필수입니다")
    private String address;
    
    private Double latitude;
    
    private Double longitude;
}

