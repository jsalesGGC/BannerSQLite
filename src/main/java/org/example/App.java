package org.example;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        BannerUtils bannerUtils = new BannerUtils();
        bannerUtils.review();
        bannerUtils.prune();
    }
}
