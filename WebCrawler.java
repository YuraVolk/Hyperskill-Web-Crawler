package crawler;

import crawler.logic.SearchThread;
import crawler.logic.Site;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

public class WebCrawler extends JFrame {
    private JTextArea textArea;
    private JTextField inputField;
    private JLabel title;

    public WebCrawler() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setTitle("Web Crawler");
        setBackground(Color.decode("#e2e2e2"));

        setLocationRelativeTo(null);
        initUserInterface();
        setLayout(null);
        setVisible(true);
    }

    private void onButtonClick() {
        ExecutorService service = Executors.newFixedThreadPool(10);
        Map<String, Site> urls = new LinkedHashMap<>();
        urls.put(inputField.getText(), new Site(inputField.getText()));
        service.submit(new SearchThread(inputField.getText(), service, urls));
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Site resultSite = urls.get(inputField.getText());
        textArea.setText(resultSite.getContent());
        title.setText(resultSite.getTitle());
    }

    private void initUserInterface() {
        JPanel buttonsContainer = new JPanel();
        buttonsContainer.setBounds(30,10,425,25);
        buttonsContainer.setLayout(new BorderLayout());
        add(buttonsContainer);

        inputField = new JTextField();
        inputField.setName("UrlTextField");
        buttonsContainer.add(inputField, BorderLayout.CENTER);

        JButton button = new JButton("Get text!");
        button.setName("RunButton");
        buttonsContainer.add(button, BorderLayout.EAST);
        button.addActionListener(e -> onButtonClick());

        JPanel titleContainer = new JPanel();
        titleContainer.setBounds(30,37,425,25);
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.X_AXIS));
        titleContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titleContainer);

        Font titleFont = new Font("Roboto", Font.BOLD, 16);
        JLabel titleLabel = new JLabel("Title: ");

        titleLabel.setFont(titleFont);
        titleContainer.add(titleLabel);
        title = new JLabel();
        title.setFont(titleFont);
        title.setName("TitleLabel");
        titleContainer.add(title);

        JPanel container = new JPanel();
        container.setBounds(30,70,425,365);
        container.setLayout(new BorderLayout());
        add(container);

        textArea = new JTextArea();
        textArea.setName("HtmlTextArea");
        textArea.setEditable(false);
        textArea.disable();
        textArea.setBorder(BorderFactory.createCompoundBorder(
                textArea.getBorder(),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        container.add(textArea);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        container.add(scroll);
    }
}