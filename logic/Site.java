package crawler.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Site {
    private final String url;
    private String title;
    private String content;

    public Site(String url) {
        this.url = url;
        this.title = "";
        this.content = "";
    }

    public void setContent(String content) {
        if (content.equals("ERROR") || content.length() == 0) {
            return;
        }

        this.content = content;
        Pattern pattern = Pattern.compile("<title>(.+?)</title>");
        Matcher matcher = pattern.matcher(this.content);
        matcher.find();
        title = matcher.group(1);
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
