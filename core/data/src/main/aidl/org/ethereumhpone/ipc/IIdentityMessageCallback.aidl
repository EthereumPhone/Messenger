package org.ethereumhpone.ipc;

oneway interface IIdentityMessageCallback {
    void onNewMessages(int messageCount);
}
