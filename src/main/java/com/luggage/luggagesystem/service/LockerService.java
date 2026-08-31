package com.luggage.luggagesystem.service;

import com.luggage.luggagesystem.entity.Locker;
import com.luggage.luggagesystem.enums.LockerStatus;

import java.util.List;

public interface LockerService {

    Locker createLocker(Locker locker);

    List<Locker> listLockers();

    Locker getLockerById(Long lockerId);

    boolean changeLockerStatus(Long lockerId, LockerStatus status);
}