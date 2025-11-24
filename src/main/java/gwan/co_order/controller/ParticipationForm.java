package gwan.co_order.controller;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
public class ParticipationForm {
    // 상품ID : 수량
    private Map<Long, Integer> productQuantities;
}