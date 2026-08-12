package com.mobilebanking.platform.transfer.service;

import com.mobilebanking.platform.account.domain.BankAccount;
import com.mobilebanking.platform.account.repository.BankAccountRepository;
import com.mobilebanking.platform.auth.security.BankingPrincipal;
import com.mobilebanking.platform.common.exception.BusinessException;
import com.mobilebanking.platform.common.exception.ErrorCode;
import com.mobilebanking.platform.common.util.Hashing;
import com.mobilebanking.platform.common.util.MoneyUtils;
import com.mobilebanking.platform.config.BankProperties;
import com.mobilebanking.platform.risk.domain.RiskEventType;
import com.mobilebanking.platform.risk.domain.RiskLevel;
import com.mobilebanking.platform.risk.service.RiskEventService;
import com.mobilebanking.platform.transfer.domain.TransferOrder;
import com.mobilebanking.platform.transfer.domain.TransferStatus;
import com.mobilebanking.platform.transfer.web.CreateTransferRequest;
import com.mobilebanking.platform.transfer.web.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TransferApplicationService {

    private static final Pattern IDEMPOTENCY_KEY_PATTERN = Pattern.compile("[A-Za-z0-9._:-]+");

    private final BankAccountRepository bankAccountRepository;
    private final TransferReservationService reservationService;
    private final TransferExecutionService executionService;
    private final TransferFailureService failureService;
    private final RiskEventService riskEventService;
    private final BankProperties bankProperties;
    private final Clock clock;

    public TransferApplicationResult create(BankingPrincipal principal,
                                            String idempotencyKey,
                                            CreateTransferRequest request,
                                            String clientIp) {
        String normalizedKey = validateIdempotencyKey(idempotencyKey);
        BigDecimal amount = MoneyUtils.normalizePositive(request.amount());
        String remark = normalizeRemark(request.remark());

        BankAccount payer = bankAccountRepository.findByAccountNumber(request.payerAccountNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "付款账户不存在"));
        BankAccount payee = bankAccountRepository.findByAccountNumber(request.payeeAccountNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, "收款账户不存在"));

        if (!payer.getUser().getId().equals(principal.userId())) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_OWNED);
        }
        if (payer.getId().equals(payee.getId())) {
            throw new BusinessException(ErrorCode.SELF_TRANSFER);
        }
        if (!payer.getCurrency().equals(payee.getCurrency())) {
            throw new BusinessException(ErrorCode.CURRENCY_MISMATCH);
        }

        String requestHash = requestHash(payer, payee, amount, remark);
        TransferReservationResult reservation = reservationService.reserve(new TransferReservationCommand(
                UUID.randomUUID().toString(),
                payer.getId(),
                payee.getId(),
                payer.getAccountNumber(),
                payee.getAccountNumber(),
                principal.userId(),
                normalizedKey,
                requestHash,
                amount,
                payer.getCurrency(),
                remark,
                clock.instant()
        ));

        TransferOrder order = reservation.order();
        if (!order.getRequestHash().equals(requestHash)) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT);
        }
        if (!reservation.created()) {
            return replay(order);
        }

        try {
            TransferOrder executed = executionService.execute(order.getId(), clientIp);
            return new TransferApplicationResult(TransferResponse.from(executed, false), true);
        } catch (BusinessException exception) {
            failureService.markFailed(order.getId(), exception.getErrorCode(), exception.getMessage(), clientIp);
            recordRiskEvent(principal.userId(), payer.getId(), order.getId(), amount, exception);
            throw exception;
        } catch (RuntimeException exception) {
            failureService.markFailed(order.getId(), ErrorCode.INTERNAL_ERROR,
                    ErrorCode.INTERNAL_ERROR.defaultMessage(), clientIp);
            riskEventService.record(principal.userId(), payer.getId(), order.getId(),
                    RiskEventType.TRANSFER_SYSTEM_ERROR, RiskLevel.HIGH, amount,
                    exception.getClass().getSimpleName());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private TransferApplicationResult replay(TransferOrder order) {
        if (order.getStatus() == TransferStatus.FAILED) {
            ErrorCode code = ErrorCode.fromName(order.getErrorCode(), ErrorCode.INTERNAL_ERROR);
            throw new BusinessException(code, order.getErrorMessage());
        }
        return new TransferApplicationResult(TransferResponse.from(order, true), false);
    }

    private String validateIdempotencyKey(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }
        String key = value.trim();
        int min = bankProperties.getTransfer().getIdempotencyKeyMinLength();
        int max = bankProperties.getTransfer().getIdempotencyKeyMaxLength();
        if (key.length() < min || key.length() > max || !IDEMPOTENCY_KEY_PATTERN.matcher(key).matches()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "Idempotency-Key 长度应为 " + min + "-" + max + "，且仅允许字母、数字、点、下划线、冒号和连字符");
        }
        return key;
    }

    private String requestHash(BankAccount payer,
                               BankAccount payee,
                               BigDecimal amount,
                               String remark) {
        String canonical = String.join("|",
                payer.getAccountNumber(),
                payee.getAccountNumber(),
                amount.toPlainString(),
                payer.getCurrency(),
                remark == null ? "" : remark);
        return Hashing.sha256(canonical);
    }

    private String normalizeRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String normalized = remark.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void recordRiskEvent(Long userId,
                                 Long accountId,
                                 String transferId,
                                 BigDecimal amount,
                                 BusinessException exception) {
        RiskEventType type;
        RiskLevel level;
        switch (exception.getErrorCode()) {
            case SINGLE_LIMIT_EXCEEDED -> {
                type = RiskEventType.SINGLE_LIMIT_EXCEEDED;
                level = RiskLevel.MEDIUM;
            }
            case DAILY_LIMIT_EXCEEDED -> {
                type = RiskEventType.DAILY_LIMIT_EXCEEDED;
                level = RiskLevel.HIGH;
            }
            case INSUFFICIENT_BALANCE -> {
                type = RiskEventType.INSUFFICIENT_BALANCE;
                level = RiskLevel.LOW;
            }
            case ACCOUNT_NOT_ACTIVE -> {
                type = RiskEventType.ACCOUNT_RESTRICTED;
                level = RiskLevel.HIGH;
            }
            default -> {
                return;
            }
        }
        riskEventService.record(userId, accountId, transferId, type, level, amount, exception.getMessage());
    }
}
