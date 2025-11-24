package gwan.co_order.policy;

import org.springframework.stereotype.Component;

@Component
public class FixedRadiusScanRangePolicy implements PostScanRangePolicy {

    // 기본 스캔 반경 (km)
    private static final double DEFAULT_RADIUS_KM = 1.0;

    // 1km ≈ 위도/경도 0.009
    private static final double KM_TO_DEGREE = 0.009;

    @Override
    public ScanRange getScanRange() {
        double range = DEFAULT_RADIUS_KM * KM_TO_DEGREE;
        return new ScanRange(range, range);
    }
}