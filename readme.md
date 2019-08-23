USB -- Ultra Simple Bank
------------------------

By design, this exercise is kept simple.

The goal of this exercise is to build an actor-based ultra simple bank (USB). 
Each account is assumed to have initial 200 euro.

APIs
To money transfer- 
POST /moneyTransfer/
GET /account/{accountId}

Future extension-
- Handling of accoount balance consistency between money transfer i.e. managing transactions by introducing intermediate status as RESERVED and COMPLETED status in account actor with timeout/failover scenarios.
- Here each AccountActor can use event-sourcing to persist all debit/credit events.
