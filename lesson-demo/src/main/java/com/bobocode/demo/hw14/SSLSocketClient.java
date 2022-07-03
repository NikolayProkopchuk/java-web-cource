package com.bobocode.demo.hw14;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SSLSocketClient {

    public static void main(String[] args) {
        Map.Entry<String, Long> largestImage = getMaxSizeImage();
        System.out.println(largestImage);
    }

    @SneakyThrows
    public static Map.Entry<String, Long> getMaxSizeImage() {
        SSLSocketFactory socketFactory =
                (SSLSocketFactory) SSLSocketFactory.getDefault();
        List<String> imagesUrls = getImagesUrls(socketFactory);
        Map<String, String> urlRedirectUrlMap = getUrlRedirectUrlMap(imagesUrls);

        return getMaxSizeImageEntry(urlRedirectUrlMap, socketFactory);
    }

    private static List<String> getImagesUrls(SSLSocketFactory socketFactory) throws IOException {
        try (Socket socket =
                     socketFactory.createSocket("api.nasa.gov", 443);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     socket.getOutputStream())))) {
            URL url = new URL("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=16&api_key=DEMO_KEY");
            sendRequest(out, url, "GET");

            if (out.checkError())
                System.out.println(
                        "SSLSocketClient:  java.io.PrintWriter error");

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = in.readLine()) != null) {
                if (line.contains("{") || line.contains("}")) {
                    stringBuilder.append(line);
                }
            }
            String body = stringBuilder.toString();
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readTree(body).findValuesAsText("img_src");
        }
    }

    private static void sendRequest(PrintWriter out, URL url, String method) {
        String queryPart = url.getQuery() != null
                ? "?" + url.getQuery()
                : "";
        out.println(method + " " + url.getPath() + queryPart + " HTTP/1.1");
        out.println("Host: " + url.getHost());
        out.println("Connection: close");
        out.println();
        out.flush();
    }

    private static Map.Entry<String, Long> getMaxSizeImageEntry(Map<String, String> urlRedirectUrlMap, SSLSocketFactory socketFactory) {
        return urlRedirectUrlMap.entrySet().parallelStream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), getImageSize(entry.getValue(), socketFactory)))
                .max(Map.Entry.comparingByValue()).orElse(null);
    }

    @SneakyThrows
    private static long getImageSize(String redirectUrl, SSLSocketFactory socketFactory) {
        URL url = new URL(redirectUrl);
        try (Socket socket = socketFactory.createSocket(url.getHost(), 443);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     socket.getOutputStream())))) {

            sendRequest(out, url, "HEAD");

            if (out.checkError())
                System.out.println(
                        "SSLSocketClient:  java.io.PrintWriter error");

            return Long.parseLong(getHeaderValue(in, "Content-Length"));
        }
    }

    private static String getHeaderValue(BufferedReader reader, String header) throws IOException {
        String line;
        String value = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(header)) {
                value = line.substring(line.indexOf(' ') + 1);
            }
        }
        return value;
    }

    private static Map<String, String> getUrlRedirectUrlMap(List<String> imagesUrls) {
        return imagesUrls.parallelStream()
                .collect(Collectors.toMap(Function.identity(), SSLSocketClient::getRedirectUrl));
    }

    @SneakyThrows
    private static String getRedirectUrl(String urlStr) {
        URL url = new URL(urlStr);
        try (Socket socket = new Socket(url.getHost(), 80);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                     new BufferedWriter(
                             new OutputStreamWriter(
                                     socket.getOutputStream())))) {
            sendRequest(out, url, "HEAD");

            return getHeaderValue(in, "Location");
        }
    }
}
