package gwan.co_order.policy;

import gwan.co_order.domain.Address;

public interface MinParticipantsPolicy {

    int calculate(Address hostAddress, Address storeAddress);
}