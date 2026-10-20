package xyz.zcraft.qcb.client.request;

import java.util.List;

/**
 * Optional fields are omitted. clearImages/clearVideos apply to editing.
 */
public record FeedContent(
        String content,
        String title,
        String markdownContent,
        String contentFile,
        Integer feedType,
        List<MediaFile> filePaths,
        List<MediaFile> videoPaths,
        List<RemoteImage> images,
        List<AtUser> atUsers,
        List<TextLink> urls,
        List<String> topicNames,
        Boolean clearImages,
        Boolean clearVideos
) {
    public FeedContent {
        filePaths = filePaths == null ? null : List.copyOf(filePaths);
        videoPaths = videoPaths == null ? null : List.copyOf(videoPaths);
        images = images == null ? null : List.copyOf(images);
        atUsers = atUsers == null ? null : List.copyOf(atUsers);
        urls = urls == null ? null : List.copyOf(urls);
        topicNames = topicNames == null ? null : List.copyOf(topicNames);
    }

    public FeedContent(String content) {
        this(content, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public FeedContent(String content, List<MediaFile> filePaths) {
        this(content, null, null, null, null, filePaths, null, null, null, null, null, null, null);
    }

    public static FeedContent markdown(String title, String markdownContent) {
        return new FeedContent(null, title, markdownContent, null, 2, null, null, null, null, null, null, null, null);
    }
}

