package com.mobilebanking.platform.account.repository;

import com.mobilebanking.platform.account.domain.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    boolean existsByAccountNumber(String accountNumber);

    @EntityGraph(attributePaths = "user")
    List<BankAccount> findAllByUserIdOrderByCreatedAtAsc(Long userId);

    @EntityGraph(attributePaths = "user")
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    @EntityGraph(attributePaths = "user")
    Page<BankAccount> findAllByOrderByIdAsc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BankAccount a join fetch a.user where a.accountNumber = :accountNumber")
    Optional<BankAccount> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from BankAccount a join fetch a.user where a.accountNumber in :accountNumbers order by a.id")
    List<BankAccount> findAllByAccountNumberInForUpdate(@Param("accountNumbers") Collection<String> accountNumbers);
}
