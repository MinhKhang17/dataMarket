package com.example.datasetapi.service.payment;

import com.example.datasetapi.model.paySystem.BankAccount;
import com.example.datasetapi.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements  BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    @Override
    public BankAccount getBankAccount(Long id) {
        return bankAccountRepository.findById(id).get();
    }
}
