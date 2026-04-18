package edu.icet.service;

import edu.icet.model.dto.ReturnDto;
import edu.icet.model.dto.ReturnItemDto;

import java.util.List;

public interface ReturnService {
    void processReturn(ReturnDto returnDto, List<ReturnItemDto> items, Integer cashierId);
    List<ReturnDto> getAllReturns();
}
