package io.realm.mongodb.sync

class ResetHelper {
    companion object {
        fun triggerClientReset(sync: Sync, session: SyncSession) {
            sync.simulateClientReset(session)
        }
    }
}