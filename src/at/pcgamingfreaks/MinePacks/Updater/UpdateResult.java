/*
 *   Copyright (C) 2016 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */package at.pcgamingfreaks.MinePacks.Updater;

/**
 * Gives the developer the result of the update process.
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
	 * For some reason, the updater was unable to contact the server to download the file.
	 */
	FAIL_SERVER_OFFLINE,
	/**
	 * When running the version check, the file did not contain a recognizable version.
	 */
	FAIL_NO_VERSION_FOUND,
	/**
	 * The update provider was unable to find the file.
	 */
	FAIL_FILE_NOT_FOUND,
	/**
	 * The server administrator has improperly configured their API key in the configuration.
	 */
	FAIL_API_KEY,
	/**
	 * The updater found an update, but the used provider doesn't provide us with a download link.
	 */
	UPDATE_AVAILABLE,
	UPDATE_AVAILABLE_V2
}