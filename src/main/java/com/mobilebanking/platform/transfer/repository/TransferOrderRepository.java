package com.mobilebanking.platform.transfer.repository;

import com.mobilebanking.platform.transfer.domain.TransferOrder;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransferOrderRepository extends JpaRepository<TransferOrder, String> {

    Optional<TransferOrder> findByPayerAccountIdAndIdempotencyKey(Long payerAccountId, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransferOrder t where t.id = :id")
    Optional<TransferOrder> findByIdForUpdate(@Param("id") String id);

    @Query("select coalesce(sum(t.amount), 0) from TransferOrder t " +
            "where t.payerAccountId = :payerAccountId and t.status = :status and t.createdAt >= :from")
    BigDecimal sumAmountByPayerAndStatusSince(@Param("payerAccountId") Long payerAccountId,
                                              @Param("status") TransferStatus status,
                                              @Param("from") Instant from);

    @Query("select t from TransferOrder t where t.initiatedByUserId = :userId " +
            "or t.payeeAccountId in :accountIds order by t.createdAt desc")
    Page<TransferOrder> findVisibleToUser(@Param("userId") Long userId,
                                          @Param("accountIds") Collection<Long> accountIds,
                                          Pageable pageable);

    Page<TransferOrder> findAllByInitiatedByUserIdOrderByCreatedAtDesc(Long initiatedByUserId,
                                                                       Pageable pageable);

    Page<TransferOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<TransferOrder> findTop100ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
            TransferStatus status,
            Instant createdBefore);
}
