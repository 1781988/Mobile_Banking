package com.mobilebanking.platform.transfer.repository;

import com.mobilebanking.platform.transfer.domain.LedgerDirection;
import com.mobilebanking.platform.transfer.domain.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    Page<LedgerEntry> findAllByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    @Query("select coalesce(sum(l.amount), 0) from LedgerEntry l " +
            "where l.accountId = :accountId and l.direction = :direction")
    BigDecimal sumAmountByAccountAndDirection(@Param("accountId") Long accountId,
                                              @Param("direction") LedgerDirection direction);
}
