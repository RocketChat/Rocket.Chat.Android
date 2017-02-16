package chat.rocket.core;

/**
 * The sync status of each model.
 */
public interface SyncState {
  int NOT_SYNCED = 0;
  int SYNCING = 1;
  int SYNCED = 2;
  int FAILED = 3;
}
