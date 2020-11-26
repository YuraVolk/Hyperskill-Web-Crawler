package crawler.logic;

class Task {
    private int depth;
    private int maxDepth;
    private String url;

    public Task(int depth, int maxDepth, String url) {
        this.depth = depth;
        this.maxDepth = maxDepth;
        this.url = url;
    }

    public int getDepth() {
        return depth;
    }

    public int getMaxDepth() {
        return 4;
    }

    public String getUrl() {
        return url;
    }
}
