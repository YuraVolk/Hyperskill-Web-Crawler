package crawler.logic;

class Task {
    private int depth;
    private String url;

    public Task(int depth, String url) {
        this.depth = depth;
        this.url = url;
    }

    public int getDepth() {
        return depth;
    }

    public int getMaxDepth() {
        return MultithreadedCrawler.maxDepth;
    }

    public String getUrl() {
        return url;
    }
}
