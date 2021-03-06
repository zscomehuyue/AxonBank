/*
 * Copyright (c) 2016. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.samples.bank.command;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.samples.bank.api.bankaccount.command.CreateBankAccountCommand;
import org.axonframework.samples.bank.api.bankaccount.command.DepositMoneyCommand;
import org.axonframework.samples.bank.api.bankaccount.command.ReturnMoneyOfFailedBankTransferCommand;
import org.axonframework.samples.bank.api.bankaccount.command.WithdrawMoneyCommand;
import org.axonframework.samples.bank.api.bankaccount.event.*;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class BankAccount {

    @AggregateIdentifier
    private String id;

    /**
     * 透支限额
     */
    private long overdraftLimit;

    /**
     * 余额
     */
    private long balanceInCents;

    @SuppressWarnings("unused")
    private BankAccount() {
    }

    @CommandHandler
    public BankAccount(CreateBankAccountCommand command) {
        apply(new BankAccountCreatedEvent(command.getBankAccountId(), command.getOverdraftLimit()));
    }

    /**
     * 存款
     */
    @CommandHandler
    public void deposit(DepositMoneyCommand command) {
        apply(new MoneyDepositedEvent(id, command.getAmountOfMoney()));
    }

    /**
     * 冲账
     */
    @CommandHandler
    public void withdraw(WithdrawMoneyCommand command) {
        if (command.getAmountOfMoney() <= balanceInCents + overdraftLimit) {
            apply(new MoneyWithdrawnEvent(id, command.getAmountOfMoney()));
        }
    }

    /**
     * 1、Debit：借记，借方。
     * 2、credit：借款，贷款。
     *
     * 记入 借记 ,消费金额
     */
    public void debit(long amount, String bankTransferId) {
        if (amount <= balanceInCents + overdraftLimit) {
            apply(new SourceBankAccountDebitedEvent(id, amount, bankTransferId));
        } else {
            apply(new SourceBankAccountDebitRejectedEvent(bankTransferId));
        }
    }

    /**
     * 赊购;赊欠;借款;贷款
     */
    public void credit(long amount, String bankTransferId) {
        apply(new DestinationBankAccountCreditedEvent(id, amount, bankTransferId));
    }

    /**
     * 退款
     */
    @CommandHandler
    public void returnMoney(ReturnMoneyOfFailedBankTransferCommand command) {
        apply(new MoneyOfFailedBankTransferReturnedEvent(id, command.getAmount()));
    }

    @EventSourcingHandler
    public void on(BankAccountCreatedEvent event) {
        this.id = event.getId();
        this.overdraftLimit = event.getOverdraftLimit();
        this.balanceInCents = 0;
    }

    @EventSourcingHandler
    public void on(MoneyAddedEvent event) {
        balanceInCents += event.getAmount();
    }

    @EventSourcingHandler
    public void on(MoneySubtractedEvent event) {
        balanceInCents -= event.getAmount();
    }
}
