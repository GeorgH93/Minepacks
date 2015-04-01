package net.gravitydevelopment.Updater;

/**
 * Allows the developer to specify the type of update that will be run.
 */
public enum UpdateType
{
    /**
     * Run a version check, and then if the file is out of date, download the newest version.
     */
    DEFAULT,
    /**
     * Don't run a version check, just find the latest update and download it.
     */
    NO_VERSION_CHECK,
    /**
     * Get information about the version and the download size, but don't actually download anything.
     */
    NO_DOWNLOAD
}