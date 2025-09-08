package com.jpmc.midascore.web;

import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.component.DatabaseConduit;
import org.springframework.web.bind.annotation.*;

@RestController
public class BalanceController {

    private final DatabaseConduit db;

    public BalanceController(DatabaseConduit db) { this.db = db; }

    @GetMapping(value = "/balance", produces = "application/json")
    public Balance balance(@RequestParam("userId") long userId) {
        UserRecord u = db.findUser(userId);
        return new Balance(u != null ? u.getBalance() : 0f);
    }
}
