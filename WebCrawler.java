package crawler;

import crawler.logic.SearchThread;
import crawler.logic.Site;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

public class WebCrawler extends JFrame {
    private JTextField inputField;
    private JLabel title;
    private DefaultTableModel dataModel;

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
        dataModel.setRowCount(0);
        long time = System.currentTimeMillis();
        ExecutorService service = Executors.newFixedThreadPool(1000);
        Map<String, Site> urls = new LinkedHashMap<>();
        urls.put(inputField.getText(), new Site(inputField.getText()));
        service.submit(new SearchThread(inputField.getText(), service, urls, 0));

        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Site firstSite = urls.get(inputField.getText());
        title.setText(firstSite.getTitle());

        System.out.println(urls);
        for (Site site : urls.values()) {
            dataModel.addRow(new Object[]{site.getUrl(), site.getTitle()});
        }
        System.out.println(System.currentTimeMillis() - time);
    }

    private void initUserInterface() {
        JPanel buttonsContainer = new JPanel();
        buttonsContainer.setBounds(30,10,425,25);
        buttonsContainer.setLayout(new BorderLayout());
        add(buttonsContainer);

        inputField = new JTextField();
        inputField.setName("UrlTextField");
        buttonsContainer.add(inputField, BorderLayout.CENTER);

        JButton button = new JButton("Parse");
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

        dataModel = new DefaultTableModel();
        dataModel.addColumn("URL");
        dataModel.addColumn("Title");
        JTable resultTable = new JTable(dataModel);
        resultTable.setName("TitlesTable");
        resultTable.setBorder(BorderFactory.createCompoundBorder(
                resultTable.getBorder(),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        resultTable.disable();
        container.add(resultTable);

        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        container.add(scroll);
    }
}