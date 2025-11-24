package gwan.co_order.policy;

import gwan.co_order.domain.Address;
import org.springframework.stereotype.Component;

@Component
public class DeliveryFeeBasedMinParticipantsPolicy implements MinParticipantsPolicy {

    // 기본 배달비 (원)
    private static final int BASE_DELIVERY_FEE = 3000;

    // km당 추가 배달비 (원)
    private static final int FEE_PER_KM = 500;

    // 목표 1인당 배달비 (원)
    private static final int TARGET_FEE_PER_PERSON = 1000;

    @Override
    public int calculate(Address hostAddress, Address storeAddress) {
        // 1. 거리 계산
        double distance = calculateDistance(hostAddress, storeAddress);

        // 2. 총 배달비 계산
        int totalDeliveryFee = BASE_DELIVERY_FEE + (int) (distance * FEE_PER_KM);

        // 3. 최소 인원 계산 (총 배달비 / 목표 1인당 배달비)
        return (int) Math.ceil((double) totalDeliveryFee / TARGET_FEE_PER_PERSON);
    }

    private double calculateDistance(Address addr1, Address addr2) {
        double lat1 = Math.toRadians(addr1.getLatitude());
        double lng1 = Math.toRadians(addr1.getLongitude());
        double lat2 = Math.toRadians(addr2.getLatitude());
        double lng2 = Math.toRadians(addr2.getLongitude());

        double dlat = lat2 - lat1;
        double dlng = lng2 - lng1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dlng / 2) * Math.sin(dlng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 대략적인 지구 반지름 (km)
        final double EARTH_RADIUS = 6371.0;

        return EARTH_RADIUS * c;
    }
}