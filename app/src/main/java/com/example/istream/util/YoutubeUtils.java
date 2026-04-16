package com.example.istream.util;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.List;

public final class YoutubeUtils {
    private YoutubeUtils() {
    }

    public static String extractVideoId(String rawUrl) {
        if (TextUtils.isEmpty(rawUrl) || !Patterns.WEB_URL.matcher(rawUrl).matches()) {
            return null;
        }

        Uri uri = Uri.parse(rawUrl.trim());
        String host = uri.getHost();
        if (host == null) {
            return null;
        }
        host = host.toLowerCase();

        if (host.contains("youtube.com")) {
            String videoId = uri.getQueryParameter("v");
            if (!TextUtils.isEmpty(videoId)) {
                return videoId;
            }
            List<String> segments = uri.getPathSegments();
            if (segments.size() >= 2 && "embed".equals(segments.get(0))) {
                return segments.get(1);
            }
            if (!segments.isEmpty() && "shorts".equals(segments.get(0)) && segments.size() >= 2) {
                return segments.get(1);
            }
        } else if (host.contains("youtu.be")) {
            List<String> segments = uri.getPathSegments();
            if (!segments.isEmpty()) {
                return segments.get(0);
            }
        }

        return null;
    }

    public static String buildEmbedHtml(String videoId) {
        return "<html><body style='margin:0;'>" +
                "<iframe width='100%' height='100%' " +
                "src='https://www.youtube.com/embed/" + videoId + "?autoplay=1' " +
                "frameborder='0' allow='autoplay; encrypted-media' allowfullscreen></iframe>" +
                "</body></html>";
    }
}
