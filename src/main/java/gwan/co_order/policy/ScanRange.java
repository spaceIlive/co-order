package gwan.co_order.policy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 스캔 범위 DTO
 */
@Getter
@RequiredArgsConstructor
public class ScanRange {
    private final double latRange;  // 위도 범위
    private final double lngRange;  // 경도 범위
}