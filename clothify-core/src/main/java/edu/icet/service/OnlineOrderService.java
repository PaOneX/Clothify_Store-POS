package edu.icet.service;

import edu.icet.model.dto.OnlineOrderRequestDto;
import edu.icet.model.dto.OnlineOrderResponseDto;
import edu.icet.model.dto.OrderDto;

public interface OnlineOrderService {
    OnlineOrderResponseDto placeOnlineOrder(OnlineOrderRequestDto request);
    OrderDto getOnlineOrderStatus(Integer orderId, String phone);
}
