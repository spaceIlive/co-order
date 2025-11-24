package gwan.co_order.controller;

import gwan.co_order.domain.Address;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberAddressForm {

    @NotEmpty(message = "주소는 필수입니다")
    private String address;

    @NotNull(message = "위도는 필수입니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    private Double longitude;

    public static MemberAddressForm from(Address address) {
        MemberAddressForm form = new MemberAddressForm();
        if (address != null) {
            form.setAddress(address.getAddress());
            form.setLatitude(address.getLatitude());
            form.setLongitude(address.getLongitude());
        }
        return form;
    }
}

