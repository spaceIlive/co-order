package gwan.co_order.controller;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Getter @Setter
public class PostForm {
    
    @NotNull(message = "가게를 선택해주세요")
    private Long storeId;
    
    @NotNull(message = "마감 시간을 입력해주세요")
    @Future(message = "마감 시간은 현재 시간보다 미래여야 합니다")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;
    
    // 상품ID : 수량
    private Map<Long, Integer> productQuantities;
}

