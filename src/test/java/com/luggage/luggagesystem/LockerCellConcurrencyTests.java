package com.luggage.luggagesystem;

import com.luggage.luggagesystem.entity.Locker;
import com.luggage.luggagesystem.entity.LockerCell;
import com.luggage.luggagesystem.enums.CellSizeType;
import com.luggage.luggagesystem.enums.CellStatus;
import com.luggage.luggagesystem.enums.LockerStatus;
import com.luggage.luggagesystem.mapper.LockerCellMapper;
import com.luggage.luggagesystem.mapper.LockerMapper;
import com.luggage.luggagesystem.service.LockerCellService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LockerCellConcurrencyTests {

    @Autowired
    private LockerCellService lockerCellService;

    @Autowired
    private LockerMapper lockerMapper;

    @Autowired
    private LockerCellMapper lockerCellMapper;

    @Test
    void onlyOneRequestCanOccupySameCell()
            throws Exception {

        Locker locker = createLocker();
        LockerCell cell = createCell(locker.getId());

        Long cellId = cell.getId();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch =
                new CountDownLatch(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        try {
            Future<Boolean> firstFuture =
                    executor.submit(() -> {
                        readyLatch.countDown();
                        startLatch.await();

                        return lockerCellService
                                .occupyCell(cellId);
                    });

            Future<Boolean> secondFuture =
                    executor.submit(() -> {
                        readyLatch.countDown();
                        startLatch.await();

                        return lockerCellService
                                .occupyCell(cellId);
                    });

            /*
             * 等待两个线程都准备完成，
             * 再让它们同时开始执行。
             */
            boolean bothReady =
                    readyLatch.await(
                            5,
                            TimeUnit.SECONDS
                    );

            assertTrue(
                    bothReady,
                    "两个并发线程没有按时准备完成"
            );

            startLatch.countDown();

            boolean firstResult =
                    firstFuture.get(
                            5,
                            TimeUnit.SECONDS
                    );

            boolean secondResult =
                    secondFuture.get(
                            5,
                            TimeUnit.SECONDS
                    );

            int successCount = 0;

            if (firstResult) {
                successCount++;
            }

            if (secondResult) {
                successCount++;
            }

            // 两个请求中必须只有一个成功
            assertEquals(1, successCount);

            LockerCell storedCell =
                    lockerCellMapper.selectById(cellId);

            assertEquals(
                    CellStatus.OCCUPIED,
                    storedCell.getStatus()
            );

            assertEquals(
                    1,
                    storedCell.getVersion()
            );
        } finally {
            executor.shutdownNow();

            /*
             * 本测试没有@Transactional，
             * 因此必须手动清理测试数据。
             * 先删除柜格，再删除寄存柜。
             */
            lockerCellMapper.deleteById(cellId);
            lockerMapper.deleteById(locker.getId());
        }
    }

    private Locker createLocker() {
        Locker locker = new Locker();

        locker.setLockerCode(
                "CONCURRENT-" + System.nanoTime()
        );
        locker.setName("并发测试寄存柜");
        locker.setLocation("测试位置");
        locker.setStatus(LockerStatus.ENABLED);

        lockerMapper.insert(locker);

        return locker;
    }

    private LockerCell createCell(Long lockerId) {
        LockerCell cell = new LockerCell();

        cell.setLockerId(lockerId);
        cell.setCellNo("CONCURRENT-01");
        cell.setSizeType(CellSizeType.SMALL);
        cell.setStatus(CellStatus.AVAILABLE);
        cell.setVersion(0);

        lockerCellMapper.insert(cell);

        return cell;
    }
}