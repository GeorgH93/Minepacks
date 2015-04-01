package net.gravitydevelopment.Updater;

/**
 * Gives the developer the result of the update process. Can be obtained by called {@link #getResult()}
 */
public enum UpdateResult
{
    /**
     * The updater found an update, and has readied it to be loaded the next time the server restarts/reloads.
     */
    SUCCESS,
    /**
     * The updater did not find an update, and nothing was downloaded.
     */
    NO_UPDATE,
    /**
     * The server administrator has disabled the updating system.
     */
    DISABLED,
    /**
     * The updater found an update, but was unable to download it.
     */
    FAIL_DOWNLOAD,
    /**
     * For some reason, the updater was unable to contact dev.bukkit.org to download the file.
     */
    FAIL_DBO,
    /**
     * When running the version check, the file on DBO did not contain a recognizable version.
     */
    FAIL_NOVERSION,
    /**
     * The id provided by the plugin running the updater was invalid and doesn't exist on DBO.
     */
    FAIL_BADID,
    /**
     * The server administrator has improperly configured their API key in the configuration.
     */
    FAIL_APIKEY,
    /**
     * The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded.
     */
    UPDATE_AVAILABLE
}