package com.xungengbang.Download;

public interface DownloadProgressListener {

    void update(long bytesRead, long contentLength, boolean done);

}
