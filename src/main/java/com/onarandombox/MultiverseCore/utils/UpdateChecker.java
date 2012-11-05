/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.MultiverseCore.utils;

import com.dumptruckman.minecraft.util.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/*
 * Apparently this isn't used and I don't know if we'll ever use this,
 * so I'm just going to deprecate it for now and suppress the warnings.
 *
 * BEGIN CHECKSTYLE-SUPPRESSION: ALL
 */

/**
 * @deprecated Currently unused.
 */
@Deprecated
public class UpdateChecker {

    public static final Logger log = Logger.getLogger("Minecraft");

    private Timer timer = new Timer(); // Create a new Timer.

    private String name; // Hold the Plugins Name.
    private String cversion; // Hold the Plugins Current Version.

    public UpdateChecker(String name, String version) {
        this.name = name;
        this.cversion = version;

        int delay = 0; // No Delay, fire the first check instantly.
        int period = 1800; // Delay 30 Minutes

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkUpdate();
            }
        }, delay * 1000, period * 1000);
    }

    public void checkUpdate() {
        BufferedReader rd = null;
        try {
            URL url = new URL("http://bukkit.onarandombox.com/multiverse/version.php?n=" + URLEncoder.encode(this.name, "UTF-8") + "&v=" + this.cversion);
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(2000); // 2000 = 2 Seconds.

            int code = ((HttpURLConnection) conn).getResponseCode();

            if (code != 200) {
                return;
            }

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            String version = null;

            while ((line = rd.readLine()) != null) {
                if (version == null) {
                    version = line;
                }
            }

            if (version == null) {
                return;
            }

            String v1 = normalisedVersion(version);
            String v2 = normalisedVersion(this.cversion);

            int compare = v1.compareTo(v2);

            if (compare > 0) {
                Logging.info("[%s] - Update Available (%s)", this.name, version);
            }

            rd.close();
        } catch (Exception e) {
            // No need to alert the user of any error here... it's not important.
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException ignore) { }
            }
        }
    }

    /**
     * Convert the given Version String to a Normalized Version String so we can compare it.
     *
     * @param version The version string
     * @return The normalized version string
     */
    public static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    public static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }

}

/*
 * END CHECKSTYLE-SUPPRESSION: ALL
 */
