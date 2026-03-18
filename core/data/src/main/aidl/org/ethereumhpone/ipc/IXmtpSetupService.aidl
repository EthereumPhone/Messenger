package org.ethereumhpone.ipc;

import org.ethereumhpone.ipc.IXmtpSetupCallback;

interface IXmtpSetupService {
    void setupNow(IXmtpSetupCallback callback);
    void setupNowWithAddress(String walletAddress, IXmtpSetupCallback callback);
}
