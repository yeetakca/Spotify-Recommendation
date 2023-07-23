package com.spotify.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class SpotifyService {

    private static final String SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1";
    private static final String CLIENT_ID = "a71823ac4def4f24949a429bf9f056df";
    private static final String CLIENT_SECRET = "58b06a61d6b74215be2fbb1eb45aa32b";
    private static final String AUTHORIZATION_URL = "https://accounts.spotify.com/api/token";

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(AUTHORIZATION_URL);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<SpotifyTokenResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, request, SpotifyTokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            SpotifyTokenResponse tokenResponse = response.getBody();
            if (tokenResponse != null) {
                return tokenResponse.getAccessToken();
            }
        }

        throw new IllegalStateException("Failed to retrieve access token from Spotify API");
    }

    public String getTrack(String trackId) throws URISyntaxException {

        String endpoint = SPOTIFY_API_BASE_URL + "/tracks/" + trackId;

        return giveOutput(endpoint);
    }

    public String getPlaylist(String playlistId) throws URISyntaxException {

        String endpoint = SPOTIFY_API_BASE_URL + "/playlists/" + playlistId;

        return giveOutput(endpoint);
    }

    public String getFeatures(String trackId) throws URISyntaxException {

        String endpoint = SPOTIFY_API_BASE_URL + "/audio-features/" + trackId;

        return giveOutput(endpoint);
    }

    public String getRecs(String trackId) throws URISyntaxException {

        ObjectMapper map = new ObjectMapper();
        JsonNode node;
        try {
            node = map.readTree(getFeatures(trackId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String endpoint = SPOTIFY_API_BASE_URL + "/recommendations?limit=" + 3 + "&seed_tracks=" + trackId + "&target_acousticness=" + node.get("acousticness") + "&target_danceability=" + node.get("danceability") + "&target_energy=" + node.get("energy") + "&target_instrumentalness=" + node.get("instrumentalness") + "&target_liveness=" + node.get("liveness") + "&target_loudness=" + node.get("loudness") + "&target_speechiness=" + node.get("speechiness") + "&target_tempo=" + node.get("tempo") + "&target_valence=" + node.get("valence");
        return giveOutput(endpoint);
    }

    public String getReccs(String playlistId) throws URISyntaxException {

        ObjectMapper map = new ObjectMapper();
        JsonNode node;
        ArrayList<String> trackIds = new ArrayList<>();
        ArrayList<String> artists = new ArrayList<>();
        float acousticness = 0;
        float danceability = 0;
        float energy = 0;
        float instrumentalness = 0;
        float liveness = 0;
        float loudness = 0;
        float speechiness = 0;
        float tempo = 0;
        float valence = 0;

        ArrayList<String> commonArtists;
        try {
            node = map.readTree(getPlaylist(playlistId));
            node.get("tracks").get("items").forEach((track) -> {
                trackIds.add(track.get("track").get("id").toString().replaceAll("\"", ""));
                artists.add(track.get("track").get("artists").get(0).get("id").toString().replaceAll("\"", ""));
            });
            for (String trackId : trackIds) {
                node = map.readTree(getFeatures(trackId));
                acousticness += Float.parseFloat(String.valueOf(node.get("acousticness")));
                danceability += Float.parseFloat(String.valueOf(node.get("danceability")));
                energy += Float.parseFloat(String.valueOf(node.get("energy")));
                instrumentalness += Float.parseFloat(String.valueOf(node.get("instrumentalness")));
                liveness += Float.parseFloat(String.valueOf(node.get("liveness")));
                loudness += Float.parseFloat(String.valueOf(node.get("loudness")));
                speechiness += Float.parseFloat(String.valueOf(node.get("speechiness")));
                tempo += Float.parseFloat(String.valueOf(node.get("tempo")));
                valence += Float.parseFloat(String.valueOf(node.get("valence")));
            }
            acousticness /= trackIds.size();
            danceability /= trackIds.size();
            energy /= trackIds.size();
            instrumentalness /= trackIds.size();
            liveness /= trackIds.size();
            loudness /= trackIds.size();
            speechiness /= trackIds.size();
            tempo /= trackIds.size();
            valence /= trackIds.size();

            commonArtists = findCommonArtist(artists);
            System.out.println(commonArtists);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String endpoint = SPOTIFY_API_BASE_URL + "/recommendations?limit=" + 3;

        if (!commonArtists.isEmpty()) {
            StringBuilder artistsParam = new StringBuilder();
            int numArtists = Math.min(5, commonArtists.size());
            for (int i = 0; i < numArtists; i++) {
                artistsParam.append(commonArtists.get(i));
                if (i != numArtists - 1) {
                    artistsParam.append("%2C");
                }
            }
            endpoint += "&seed_artists=" + artistsParam;
        }

        endpoint += "&target_acousticness=" + acousticness + "&target_danceability=" + danceability + "&target_energy=" + energy + "&target_instrumentalness=" + instrumentalness + "&target_liveness=" + liveness + "&target_loudness=" + loudness + "&target_speechiness=" + speechiness + "&target_tempo=" + tempo + "&target_valence=" + valence;
        System.out.println(endpoint);
        return giveOutput(endpoint);
    }

    private ArrayList<String> findCommonArtist(ArrayList<String> artists) {
        int n = artists.size();
        HashMap<String, Integer> artistCount = new HashMap<>();

        for (String artist : artists) {
            artistCount.put(artist, artistCount.getOrDefault(artist, 0) + 1);
        }

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(artistCount.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        ArrayList<String> commonArtists = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            commonArtists.add(entry.getKey());
            count++;
            if (count == Math.min(5, sortedEntries.size())) {
                break;
            }
        }

        return commonArtists;
    }

    private String giveOutput(String endpoint) throws URISyntaxException {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        RequestEntity<?> request = new RequestEntity<>(headers, HttpMethod.GET, new URI(endpoint));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to retrieve track information");
        }
    }
}
