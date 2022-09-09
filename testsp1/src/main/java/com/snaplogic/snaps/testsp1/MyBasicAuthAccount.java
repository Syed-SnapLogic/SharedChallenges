package com.snaplogic.snaps.testsp1;

import com.snaplogic.account.api.AccountType;
import com.snaplogic.account.api.capabilities.AccountCategory;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.snap.api.account.basic.BasicAuthAccount;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Version;

@General(title = "My Basic Auth Snap Account")
@Version(snap = 1)
@AccountCategory(type = AccountType.BASIC_AUTH)
public class MyBasicAuthAccount extends BasicAuthAccount<String> {
    @Override
    public String connect() throws ExecutionException {
        return "success";
    }

    @Override
    public void disconnect() throws ExecutionException {
        // NO OP
    }
}
